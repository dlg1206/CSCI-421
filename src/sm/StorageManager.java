package sm;


import dataTypes.DataType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
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
        this.buffer = new PageBuffer(bufferSize, pageSize);
        this.pageSize = pageSize;
        this.bufferSize = bufferSize;
        this.databaseLocation = databasePath;
        StorageManager.databasePath = databasePath;
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

    // CREATE
    public void insertRecord(int tableID, List<DataType> record) throws IOException {
        // create file if DNE
        File tableFile = new File(databasePath + "/" + tableID + ".db");
        if( !tableFile.exists() )
            Files.write(tableFile.toPath(), new byte[0]);
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
