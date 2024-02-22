package sm;


import catalog.Attribute;
import dataTypes.DataType;

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
    private static String databasePath;

    private final int pageSize;
    private final int bufferSize;
    private final String databaseLocation;


    /**
     * Create a new Storage Manager with a page buffer
     *
     * @param bufferSize Max buffer size in number of pages
     * @param pageSize Max page size in number of records
     * @param databasePath Path to database directory
     */
    public StorageManager(int bufferSize, int pageSize, String databasePath){
        this.buffer = new PageBuffer(bufferSize, pageSize, databasePath);
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
        this.databaseLocation = databasePath;
        StorageManager.databasePath = databasePath;
    }


    private int getPrimaryKeyIndex(List<Attribute> attributes){
        for( int i = 0; i < attributes.size(); i ++){
            if(attributes.get(i).isPrimaryKey())
                return i;
        }
        return -1; // err, but that won't happen :)
    }

    public int getPageCount(int tableID){
        // TODO
        return -1;
    }

    public int getPageSize(){
        return this.pageSize;
    }

    public int getBufferSize(){
        return this.bufferSize;
    }

    public String getDatabaseLocation(){
        return this.databaseLocation;
    }

    public int getCountOfRecords(int tableID){
        List<List<DataType>> allRecords = getAllRecords(tableID);
        return allRecords.size();
    }

    /**
     * CREATE
     *
     * Insert record into table file
     *
     * @param tableID ID of table file
     * @param attributes Constraints of data types
     * @param record record contents
     * @throws IOException failed to write to file
     */
    public void insertRecord(int tableID, List<Attribute> attributes, List<DataType> record) throws IOException {

        TableFile tf = new TableFile(databasePath, tableID);
        int pageCount = tf.readPageCount();
        int pki = getPrimaryKeyIndex(attributes);


        // If no records, just add to page
        if( pageCount == 0 ){
            List<List<DataType>> records = new ArrayList<>();
            records.add(record);
            this.buffer.fullWrite(tf, 0, BInterpreter.convertRecordsToPage(records));
            return;
        }

        // Iterate through all pages and attempt to insert the record
        for( int pageNumber = 0; pageNumber < pageCount; pageNumber++){
            Page page = this.buffer.readFromBuffer(tableID, pageNumber, false);
            boolean recordInserted = page.insertRecord(pki, attributes, record);
            // Record added, split if needed and break
            if(recordInserted && page.isOverfull()) {
                tf.splitPage(this.buffer, pageNumber, attributes);
                break;
            }

            // Reach end of pages and not inserted, append to end and split if needed
            if(!recordInserted && pageNumber == pageCount - 1){
                page.appendRecord(attributes, record);
                if (page.isOverfull())
                    tf.splitPage(this.buffer, pageNumber, attributes);
            }
        }
    }

    // READ
    public List<DataType> getRecord(int tableID, DataType primaryKey){
        // TODO
        return null;
    }

    // READ
    public List<List<DataType>> getAllRecords(int tableID){
        /*
        List<List<DataType>> records = new ArrayList<>()
        int numPages = getPageCount(tableID)
        for i=0, i<numPages, i++
            Page p = this.buffer.getPage(tableID, i);
            records.addAdd(p.getAllRecords())
         return records
         */
        // TODO
         return null;
    }




     // UPDATE
    public void updateRecord(int tableID, DataType primaryKey, List<DataType> record){
        // TODO
    }

    // DELETE
    public void deleteRecord(int tableID, DataType primaryKey){
        // TODO
    }

    public void deleteTable(int tableID) throws IOException {
        this.buffer.flush();
        TableFile tf = new TableFile(databasePath, tableID);
        tf.delete();
    }

}
