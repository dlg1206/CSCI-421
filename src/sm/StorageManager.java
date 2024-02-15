package sm;


import dataTypes.DataType;

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


    /**
     * Create a new Storage Manager with a page buffer
     *
     * @param bufferSize Max buffer size in number of pages
     * @param pageSize Max page size in number of records
     * @param databasePath Path to database directory
     */
    public StorageManager(int bufferSize, int pageSize, String databasePath){
        this.buffer = new PageBuffer(bufferSize, pageSize);
        StorageManager.databasePath = databasePath;
    }


    private int getPageCount(int tableID){
        // TODO
        return -1;
    }

    // CREATE
    public void insertRecord(int tableID, List<DataType> record){
        // TODO
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

}
