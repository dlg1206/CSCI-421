package sm;

import catalog.Attribute;
import dataTypes.DataType;
import util.BPlusTree.RecordPointer;
import cli.cmd.exception.ExecutionFailure;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <b>File:</b> Page.java
 * <p>
 * <b>Description:</b> Utility Page object to hold page data
 *
 * @author Derek Garcia, Ryan Nowak
 */
public class Page {

    private final DBFile writeFile;
    private final int pageSize;
    private final int pageNumber;
    private byte[] data;
    public boolean IsIndexPage;

    /**
     * Create new Page
     *
     * @param writeFile  File to write page to
     * @param pageSize   Max number of records page can hold
     * @param pageNumber Page Number
     * @param data       Page byte data
     */
    public Page(DBFile writeFile, int pageSize, int pageNumber, byte[] data, boolean isIndexPage) {
        this.writeFile = writeFile;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.data = new byte[pageSize];
        IsIndexPage = isIndexPage;

        System.arraycopy(data, 0, this.data, 0, data.length);   // copy existing data
    }

    /**
     * Test if other page is the same as this
     *
     * @param otherTableID    Table ID of the other page
     * @param otherPageNumber Page number of the other page
     * @return true if match, false otherwise
     */
    public boolean match(int otherTableID, int otherPageNumber, boolean findIndexFile) {
        if (findIndexFile)
            return this.writeFile.getTableID() == otherTableID
                    && this.pageNumber == otherPageNumber
                    && this.writeFile.isIndex();
        else
            return this.writeFile.getTableID() == otherTableID
                    && this.pageNumber == otherPageNumber
                    && !this.writeFile.isSwap()
                    && !this.writeFile.isIndex();    // cannot read from swap
    }

    /**
     * Insert a record to the page
     *
     * @param primaryKeyIndex Index of the primary key to sort by
     * @param record          record to insert
     * @return Record Pointer to new record, null if not inserted
     */
    public RecordPointer insertRecord(int primaryKeyIndex, List<Attribute> attributes, List<DataType> record) throws ExecutionFailure {
        // Get records
        List<List<DataType>> records = BInterpreter.convertPageToRecords(this.data, attributes);

        // Ordered insert
        for (List<DataType> storedRecord : records) {

            int order = record.get(primaryKeyIndex).compareTo(storedRecord.get(primaryKeyIndex));
            // == 0 means same value
            if(order == 0)
                throw new ExecutionFailure("Duplicate primary key '%s'".formatted(record.get(primaryKeyIndex).stringValue()));

            // > 0 means record is less than stored
            if (order > 0) {
                records.add(records.indexOf(storedRecord), record);     // [..., stored, ...] -> [..., new, stored, ...]
                this.data = BInterpreter.convertRecordsToPage(records);
                return new RecordPointer(this.pageNumber, records.indexOf(record));
            }
        }

        // Record wasn't added
        return null;
    }

    /**
     * Delete a record from the page
     *
     * @param primaryKeyIndex Index of the primary key
     * @param primaryKey      PrimaryKey of record to delete
     * @return True if deleted, false otherwise
     */
    public boolean deleteRecord(int primaryKeyIndex, List<Attribute> attributes, DataType primaryKey) {
        // Get records
        List<List<DataType>> records = BInterpreter.convertPageToRecords(this.data, attributes);

        // Search for record to delete
        for (List<DataType> storedRecord : records) {
            // Record exists in page, so delete it
            if (primaryKey.compareTo(storedRecord.get(primaryKeyIndex)) == 0) {
                records.remove(storedRecord);
                this.data = BInterpreter.convertRecordsToPage(records);
                return true;
            }
        }

        // No record was deleted
        return false;
    }

    /**
     * Delete a record from the page (Note: the record is assumed to exist)
     *
     * @param attributes    Constraints of dataTypes
     * @param index         Index of the record to remove
     */
    public void deleteRecordByIndex(List<Attribute> attributes, int index) {
        // Get records
        List<List<DataType>> records = BInterpreter.convertPageToRecords(this.data, attributes);

        records.remove(records.get(index));
        this.data = BInterpreter.convertRecordsToPage(records);
    }

    /**
     * Append record to end of page
     * SHOULD ONLY BE USED IF LAST PAGE
     *
     * @param record record to append
     * @return Record Pointer to new record
     */
    public RecordPointer appendRecord(List<Attribute> attributes, List<DataType> record) {
        List<List<DataType>> records = BInterpreter.convertPageToRecords(this.data, attributes);
        records.add(record);
        this.data = BInterpreter.convertRecordsToPage(records);
        return new RecordPointer(this.pageNumber, records.indexOf(record));
    }

    public int indexOf(List<Attribute> attributes, List<DataType> record) {
        List<List<DataType>> records = BInterpreter.convertPageToRecords(this.data, attributes);
        return records.indexOf(record);
    }


    /**
     * Split the current page into 2. The first half will remain in the current page
     *
     * @return the second half of the page
     */
    public Page split(List<Attribute> attributes) {
        List<List<DataType>> leftRecords = BInterpreter.convertPageToRecords(this.data, attributes);

        // Split right from all records
        List<List<DataType>> rightRecords = new ArrayList<>(leftRecords.subList(leftRecords.size() / 2, leftRecords.size()));

        // Create second page
        Page rightPage = new SwapPage(
                this.writeFile,
                this.pageSize,
                this.pageNumber,
                BInterpreter.convertRecordsToPage(rightRecords)
        );

        // Remove right page from this page
        leftRecords.subList(leftRecords.size() / 2, leftRecords.size()).clear();
        this.data = BInterpreter.convertRecordsToPage(leftRecords);

        return rightPage;
    }


    /**
     * Get a copy of this page with a modified number
     *
     * @param pageOffset Offset of page in swap file
     * @return new Swap Page
     * @throws IOException Failed to get swap file
     */
    public SwapPage getSwapPage(int pageOffset) throws IOException {
        return new SwapPage(
                writeFile.getSwapFile(),
                this.pageSize,
                this.pageNumber + pageOffset,
                this.data
        );
    }

    /**
     * Check if the page is above capacity
     *
     * @return true if overfull, false otherwise
     */
    public boolean isOverfull() {
        return this.data.length > this.pageSize;
    }

    /**
     * Check if the page is empty
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        // get number of records in page
        ByteBuffer numRecBuff = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4));
        int numRecords = numRecBuff.getInt();

        return numRecords == 0;
    }

    /**
     * @return Page write file
     */
    public DBFile getWriteFile() {
        return this.writeFile;
    }

    /**
     * @return Page Number
     */
    public int getPageNumber() {
        return this.pageNumber;
    }

    /**
     * @return Page Size
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * @return Page byte data
     */
    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] newData) {
        this.data = newData;
    }


}