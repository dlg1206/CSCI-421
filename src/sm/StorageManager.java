package sm;


import catalog.Attribute;
import dataTypes.DataType;

import java.io.IOException;
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

    private void splitTableFile(TableFile df, int splitPageNum){
        /*
        TableSwapFile dsf = df.createSwapFile();
        int pageCount = df.getPageCount();
        int swpPageNum = 0;
        for( int pageNum = 0; pageNum < pageCount; pageNum++){
            Page page = this.buffer.readFromBuffer(df.getTableID, pageNum);

            // add split page
            if( pageNum == splitPageNum )
                this.buffer.writeToBuffer( new Page ( ... page.split(), swpPageNum++ ) );

            // add rest of page
            this.buffer.writeToBuffer( new Page ( ... page, swpPageNum++ ) );

        }

        dsf.close();            // closes swap and overwrites old
        this.buffer.flush();    // remove any conflict data

         */
    public int getPageCount(int tableID){
        // TODO
        return -1;
    }

    private int getPrimaryKeyIndex(List<Attribute> attributes){
        for( int i = 0; i < attributes.size(); i ++){
            if(attributes.get(i).isPrimaryKey())
                return i;
        }
        return -1; // err, but that won't happen :)
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
    public void insertRecord(int tableID, List<Attribute> attributes, List<DataType> record) throws IOException {

        TableFile df = new TableFile(databasePath, tableID);

        int pageCount = df.getPageCount();
        int pki = getPrimaryKeyIndex(attributes);

        // If no records, just add to page
        if( pageCount == 0 ){
            Page page = this.buffer.createNewPage(tableID, 0);
            page.insertRecord(pki, record);
            this.buffer.writeToBuffer(page);
            return;
        }

        // Iterate through all pages and attempt to insert the record
        for( int pageNum = 0; pageNum < pageCount; pageNum++){
            Page page = this.buffer.readFromBuffer(tableID, pageNum);
            /*
            boolean recordInserted = page.insertRecord(record);
            if( recordInserted && page.isOverfull() )
                splitTableFile(pageNum)

            if( recordInserted )
                break;
             */
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

}
