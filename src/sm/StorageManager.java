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

        private record Page(int tableID, int number, Byte[] data){}

        private final List<Page> buffer;
        private final int capacity;
        private final int pageSize;

        public PageBuffer(int bufferSize, int pageSize){
            this.buffer = Arrays.asList(new Page[bufferSize]);
            this.capacity = bufferSize;
            this.pageSize = pageSize;
        }

        private void readToBuffer(int tableID, int pageNum){
            /*
            // Make room if needed
            if this.buffer.size() == this.capacity
                this.buffer.remove( this.capacity - 1 );

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
            /*
            Page page = null;     // assume not in buf
            // Check buffer for page
            for p : this.buffer
                if p.tableId == tableID && p.number == pageNumber
                    page = p
                    break

             // Update access if in buffer
             if page != null
                this.buffer.remove(page)
                this.buffer.add(page)

             return page;
             */
            return null;
        }

        private boolean isPageInBuffer(int tableID, int pageNum){
            /*
            for p : this.buffer
                if p.tableId == tableID && p.number == pageNumber
                    return true
             */
            return false;
        }


        public Byte[] getPage(int tableID, int pageNum){
            /*
            if( !isPageInBuffer(tableID, pageNum) )
                readToBuffer(tableID, pageNum);

            return readFromBuffer(tableID, pageNum).data;

             */
            return null;
        }

    }



     private int getPageCount(int tableID){
         return 1;
     }

     private Byte[] getPage(int tableID, int pageNum){
         return null;
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
