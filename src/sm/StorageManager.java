package sm;

import sm.tmp.DataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <b>File:</b> StorageManager.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class StorageManager {

    private class PageBuffer{

        private record Page(int tableID, int number, Byte[] data){
            public int getNumberOfRecords(){
                // parse data byte array to get number of records
                return -1;
            }

            public Byte[] getRecordBytes(){
                // truncate # records from data byte array
                return null;
            }
        }

        private final List<Page> buffer;
        private final int capacity;
        private final int pageSize;

        public PageBuffer(int bufferSize, int pageSize){
            this.buffer = Arrays.asList(new Page[bufferSize]);
            this.capacity = bufferSize;
            this.pageSize = pageSize;
        }

        private void writeToDisk(int tableID, Page page){
            // write to disk
        }

        private void readToBuffer(int tableID, int pageNum){
            /*
            // Make room if needed
            if this.buffer.size() == this.capacity{
                writeToDisk(
                    tableID,
                    this.buffer.remove( this.capacity - 1 )
                );
            }


            // Read new page into buffer. Add to first b/c assume going to be accessed
            int offset = pageCount + pageSize * pageNum;
            this.buffer.add( new Page(
                tableID,
                pageNum,
                sys.loadBytes(tableID) + offset;  // ie load 1 page however it's done
                )
            );
             */
        }

        private Page readFromBuffer(int tableID, int pageNum){

            Page page = null;     // assume not in buffer

            // Check buffer for page
            for( Page p : this.buffer ){
                // Find page
                if(p.tableID == tableID && p.number == pageNum){
                    page = p;
                    break;
                }
            }

            // Update access if in buffer
            this.buffer.remove(page);
            this.buffer.add(page);

            return page;
        }

        private boolean isPageInBuffer(int tableID, int pageNum){

            for( Page p : this.buffer ){
                // Find page
                if(p.tableID == tableID && p.number == pageNum)
                    return true;
            }

            return false;
        }

        public Byte[] getPage(int tableID, int pageNum){
            // Read page from disk if not in buffer
            if( !isPageInBuffer(tableID, pageNum) )
                readToBuffer(tableID, pageNum);

            return readFromBuffer(tableID, pageNum).data;
        }


    }


    private final PageBuffer buffer;
    private static String databasePath;


    public StorageManager(int bufferSize, int pageSize, String databasePath){
        this.buffer = new PageBuffer(bufferSize, pageSize);
        StorageManager.databasePath = databasePath;
    }

    private int getPageCount(int tableID){
        return 1;
    }

    // CREATE
    public void insertRecord(int tableID, DataType primaryKey, List<DataType> record){

    }

    // READ
    public List<DataType> getRecord(int tableID, DataType primaryKey){
        return null;
    }

    // READ
    public List<List<DataType>> getAllRecords(){
         return null;
    }


     // UPDATE
    public void updateRecord(int tableID, DataType primaryKey, List<DataType> record){

    }

    // DELETE
    public void deleteRecord(int tableID, DataType primaryKey){

    }

}
