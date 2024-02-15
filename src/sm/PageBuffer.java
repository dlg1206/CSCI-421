package sm;

import dataTypes.DataType;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> PageBuffer.java
 * <p>
 * <b>Description:</b> page buffer used by Storage Manager to read and write to hardware
 *
 * @author Derek Garcia
 */
class PageBuffer{

    /**
     * Utility Page object to hold metadata about page
     *
     * @param tableID Table ID of the page
     * @param number Page Number
     * @param data Raw binary data of the page
     */
    private record Page(int tableID, int number, Byte[] data){
        private int getNumberOfRecords(){
            // parse data byte array to get number of records
            return -1;
        }

        private Byte[] getRecordBytes(){
            // truncate # records from data byte array
            return null;
        }

        public List<List<DataType>> getAllRecords(){
            // parse bytes into records and return
            return null;
        }
    }

    private final List<Page> buffer = new ArrayList<>();
    private final int capacity;
    private final int pageSize;


    /**
     * Create a new Page Buffer
     *
     * @param bufferSize Max buffer size in number of pages
     * @param pageSize Max page size in number of records
     */
    public PageBuffer(int bufferSize, int pageSize){
        this.capacity = bufferSize;
        this.pageSize = pageSize;
    }

    /**
     * Check if page is in the buffer
     *
     * @param tableID Table ID to read from
     * @param pageNum Page number to read from
     * @return true if in buffer, false otherwise
     */
    private boolean isPageInBuffer(int tableID, int pageNum){

        for( Page p : this.buffer ){
            // Find page
            if(p.tableID == tableID && p.number == pageNum)
                return true;
        }

        return false;
    }

    private void writeToDisk(Page page){
        // TODO - need table path?
        // write to disk
    }


    /**
     * Read Page binary from Table file from disk to buffer
     *
     * @param tableID Table ID to read from
     * @param pageNum Page number to read from
     */
    private void readFromDisk(int tableID, int pageNum){

        // Make room if needed
        if(this.buffer.size() == this.capacity)
            writeToDisk(this.buffer.remove( this.capacity - 1 ));

        // Read new page into buffer. Add to first b/c assume going to be accessed
        int offset = this.pageSize * pageNum;   // todo add offset for table/page metadata?
//            this.buffer.add( new Page(
//                tableID,
//                pageNum,
//                sys.loadBytes(tableID) + offset  // ie load 1 page however it's done
//                )
//            );

    }

    /**
     * Write page to buffer
     *
     * @param page Page to add to buffer
     */
    public void writeToBuffer(Page page){

        // Make room if needed
        if(this.buffer.size() == this.capacity)
            writeToDisk(this.buffer.remove( this.capacity - 1 ));

        // Push list
        this.buffer.add(0, page);
    }

    /**
     * Read page from the Page Buffer
     *
     * @param tableID Table ID to read from
     * @param pageNum Page number to read from
     * @return page
     */
    public Page readFromBuffer(int tableID, int pageNum){

        Page page = null;     // assume not in buffer

        // Read page from disk if not in buffer
        // set to first to reduce search time
        if( !isPageInBuffer(tableID, pageNum) )
            readFromDisk(tableID, pageNum);

        // Get page from buffer
        for( Page p : this.buffer ){
            // Find page
            if(p.tableID == tableID && p.number == pageNum){
                page = p;
                break;
            }
        }

        // Push to top of buffer
        this.buffer.remove(page);
        this.buffer.add(0, page);

        return page;
    }

}
