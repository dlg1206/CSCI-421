package sm;


import catalog.Attribute;
import cli.cmd.exception.ExecutionFailure;
import dataTypes.DataType;
import util.BPlusTree.RecordPointer;
import util.where.WhereTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> StorageManager.java
 * <p>
 * <b>Description:</b> Handles read write operations with hardware
 *
 * @author Derek Garcia
 */
public class StorageManager {
    /*
     ┌―――――――――――――――――――[FULL WRITE]―――――――――――――――――┐
     ╭―――――╮                      ╭―――――╮                    ╭―――――╮
     │        │ ──[writeToBuffer]──> │        │ ──[writeToDisk]──> │        │
     │   SM   │                      │   PB   │                    │  DISK  │
     │        │ <─[readFromBuffer]── │        │ <─[readFromDisk]── │        │
     ╰―――――╯                      ╰―――――╯                    ╰―――――╯
     └―――――――――――――――――――[FULL READ]――――――――――――――――――┘
     */
    private final PageBuffer buffer;
    private final int pageSize;
    private final int bufferSize;
    private final String databaseRoot;
    private final boolean isIndexed;


    /**
     * Create a new Storage Manager with a page buffer
     *
     * @param bufferSize   Max buffer size in number of pages
     * @param pageSize     Max page size in number of records
     * @param databasePath Path to database directory
     * @param isIndexed    Boolean whether to use an index or not
     */
    public StorageManager(int bufferSize, int pageSize, String databasePath, boolean isIndexed) {
        this.buffer = new PageBuffer(bufferSize, pageSize, databasePath);
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
        this.databaseRoot = databasePath;
        this.isIndexed = isIndexed;
    }


    /**
     * Utility for attribute, iterates through the attribute list
     * until it finds the index of the primary key
     *
     * @param attributes list of attributes to iterate through
     * @return index of primary key, -1 if error
     */
    private int getPrimaryKeyIndex(List<Attribute> attributes) {
        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.get(i).isPrimaryKey())
                return i;
        }
        return -1; // err, but that won't happen :)
    }

    /**
     * Internal insert record method that returns a pointer to a record for B+ Trees
     *
     * @param tf         Table file to insert into
     * @param attributes Constraints of data types
     * @param record     record contents
     * @return Record pointer to the page and index of the newly inserted record
     * @throws IOException Failed to read or write to file
     */
    private RecordPointer insertRecord(TableFile tf, List<Attribute> attributes, List<DataType> record) throws IOException {
        int pageCount = tf.readPageCount();
        int pki = getPrimaryKeyIndex(attributes);
        RecordPointer recordPointer = null;

        // If no records, just add to page
        if (pageCount == 0) {
            List<List<DataType>> records = new ArrayList<>();
            records.add(record);
            this.buffer.fullWrite(tf, 0, BInterpreter.convertRecordsToPage(records));
            return new RecordPointer(0, 0);
        }

        // Iterate through all pages and attempt to insert the record
        for (int pageNumber = 0; pageNumber < pageCount; pageNumber++) {
            // read page from buffer and attempt to insert
            Page page = this.buffer.readFromBuffer(tf.getTableID(), pageNumber, false);
            recordPointer = page.insertRecord(pki, attributes, record);

            // Record added, split if needed
            if (recordPointer != null && page.isOverfull()) {
                page = this.buffer.readFromBuffer(tf.getTableID(), pageNumber, true);
                recordPointer = tf.splitPage(this.buffer, pageNumber, attributes, page, record);
            }

            // Record added, return pointer
            if (recordPointer != null)
                break;

            // Reach end of pages and not inserted, append to end and split if needed
            if (pageNumber == pageCount - 1) {
                recordPointer = page.appendRecord(attributes, record);
                if (page.isOverfull()) {
                    page = this.buffer.readFromBuffer(tf.getTableID(), pageNumber, true);
                    recordPointer = tf.splitPage(this.buffer, pageNumber, attributes, page, record);
                }
            }
        }

        return recordPointer;
    }

    //
    // CREATE
    //

    /**
     * Insert record into table file
     *
     * @param tableID    ID of table file
     * @param attributes Constraints of data types
     * @param record     record contents
     * @throws IOException failed to write to file
     */
    public void insertRecord(int tableID, List<Attribute> attributes, List<DataType> record) throws IOException, ExecutionFailure {
        // Get table file details
        TableFile tf = new TableFile(this.databaseRoot, tableID);
        RecordPointer rp = insertRecord(tf, attributes, record);

        // If index enabled, insert result
        if(this.isIndexed)
            tf.getIndex().insertPointer(this.buffer, rp);

    }


    //
    // READ
    //

    /**
     * NOTE: This method will fail if the where tree has multiple tables attached to it, this expects an algebraic select to be passed into it.
     *
     * @param tableID    TableId to get records from
     * @param attributes Constants of data types
     * @param whereTree  WhereTree to act as an algebraic select clause
     * @return List of records that pass the select clause
     */
    public List<List<DataType>> selectRecords(int tableID, List<Attribute> attributes, WhereTree whereTree) throws ExecutionFailure {
        try {
            // Get page details
            TableFile tf = new TableFile(this.databaseRoot, tableID);
            int pageCount = tf.readPageCount();

            // Get all records
            List<List<DataType>> records = new ArrayList<>();
            for (int pageNumber = 0; pageNumber < pageCount; pageNumber++) {
                Page page = this.buffer.readFromBuffer(tableID, pageNumber, false);
                List<List<DataType>> readRecords = BInterpreter.convertPageToRecords(page.getData(), attributes);
                List<List<DataType>> goodRecords = new ArrayList<>();

                for (List<DataType> record : readRecords) {
                    if (whereTree.passesTree(record))
                        goodRecords.add(record);
                }

                records.addAll(goodRecords);
            }

            return records;
        } catch (Exception e) {
            throw new ExecutionFailure("Failed to read records from table file: " + e.getMessage());
        }

    }


    /**
     * Get all records for a given table file
     *
     * @param tableID    Table ID to get records from
     * @param attributes Constraints of data types
     * @return List of records for a given table file
     */
    public List<List<DataType>> getAllRecords(int tableID, List<Attribute> attributes) throws ExecutionFailure {
        try {
            // Get page details
            TableFile tf = new TableFile(this.databaseRoot, tableID);
            int pageCount = tf.readPageCount();

            // Get all records
            List<List<DataType>> records = new ArrayList<>();
            for (int pageNumber = 0; pageNumber < pageCount; pageNumber++) {
                Page page = this.buffer.readFromBuffer(tableID, pageNumber, false);
                records.addAll(BInterpreter.convertPageToRecords(page.getData(), attributes));
            }

            return records;
        } catch (Exception e) {
            throw new ExecutionFailure("Failed to read records from table file: " + e.getMessage());
        }

    }


    /**
     * Get the page count of a table
     *
     * @param tableID Table to get page count from
     * @return number of pages, -1 if error
     */
    public int getPageCount(int tableID) {
        try {
            return new TableFile(this.databaseRoot, tableID).readPageCount();
        } catch (Exception e) {
            // todo handle?
            return -1;
        }
    }

    /**
     * @return Size of a page in bytes
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * @return Number of pages the buffer can hold
     */
    public int getBufferSize() {
        return this.bufferSize;
    }

    /**
     * @return Root directory of the database
     */
    public String getDatabaseRoot() {
        return this.databaseRoot;
    }


    //
    // UPDATE
    //
    public void updateRecord(int tableID, DataType primaryKey, List<DataType> record) {
        // TODO
    }

    //
    // DELETE
    //
    public void deleteRecord(int tableID, DataType primaryKey, List<Attribute> attributes) throws IOException {
        // Get table file details
        TableFile tf = new TableFile(this.databaseRoot, tableID);
        int pageCount = tf.readPageCount();
        int pki = getPrimaryKeyIndex(attributes);


        // read each table page in order from the table file
        for (int pageNumber = 0; pageNumber < pageCount; pageNumber++) {
            // read page from buffer and attempt to delete
            Page page = this.buffer.readFromBuffer(tableID, pageNumber, false);
            boolean recordDeleted = page.deleteRecord(pki, attributes, primaryKey);


            // Record deleted, delete page if empty
            if (recordDeleted && page.isEmpty()) {
                tf.deletePage(this.buffer, pageNumber);
            }

            // Record deleted, done
            if (recordDeleted) break;
        }

        // Delete from index if in use
        if(this.isIndexed)
            tf.getIndex().deletePointer(primaryKey);
    }

    /**
     * Drop a table from the database
     *
     * @param tableID Table to drop
     * @throws IOException Failed to read table file
     */
    public void dropTable(int tableID) throws IOException {
        this.buffer.flush();
        new TableFile(this.databaseRoot, tableID).delete();
    }

    public void flush() throws IOException {
        this.buffer.flush();
    }

}
