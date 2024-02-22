package sm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
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
    private final List<Page> buffer = new ArrayList<>();
    private final int capacity;
    private final int pageSize;
//    private final Page.PageFactory pf;
    private final String databaseRoot;



    /**
     * Create a new Page Buffer
     *
     * @param capacity Max buffer size in number of pages
     * @param pageSize Max page size in number of records
     */
    public PageBuffer(int capacity, int pageSize, String databaseRoot){
        this.capacity = capacity;
        this.pageSize = pageSize;
//        this.pf = new Page.PageFactory(this.pageSize);
        this.databaseRoot = databaseRoot;
    }


    private Page searchBuffer(int tableID, int pageNum){

        for( Page p : this.buffer ){
            // Find page
            if(p.match(tableID, pageNum))
                return p;
        }

        return null;
    }

    private void writeToDisk(Page page) throws IOException {
        TableFile writeFile = page.getWriteFile();

        int pageCount = writeFile.readPageCount();
        try( RandomAccessFile raf = writeFile.toRandomAccessFile() ){
            raf.seek(1 + (long) page.getPageNumber() * this.pageSize);
            raf.write(page.getData());
            raf.seek(0);
            raf.write((int) ((raf.length() - 1) / this.pageSize));
        }

//        try (InputStream is = new FileInputStream(writeFile.toFile())) {
//            var foo = is.readAllBytes();
//            var i =0;
//        }


    }


    /**
     * Read Page binary from Table file from disk to buffer
     *
     * @param tableID Table ID to read from
     * @param pageNumber Page number to read from
     */
    private void readFromDisk(int tableID, int pageNumber) throws IOException {
        TableFile writeFile = new TableFile(this.databaseRoot, tableID);
        byte[] buffer = new byte[this.pageSize];

        try (InputStream is = new FileInputStream(writeFile.toFile())) {
            var foo = is.readAllBytes();
            var i = 0;
        }

        try( RandomAccessFile raf = writeFile.toRandomAccessFile() ){
            raf.seek( 1 + (long) pageNumber * this.pageSize );
            raf.read(buffer, 0, this.pageSize);
        }
//        try (InputStream is = new FileInputStream(writeFile.toFile())) {
//            var foo = is.readAllBytes();
//            var i =0;
//        }


        writeToBuffer(new Page(writeFile, this.pageSize, pageNumber, buffer));

    }

    /**
     * Write page to buffer
     *
     * @param page Page to add to buffer
     */
    public void writeToBuffer(Page page) throws IOException {

        // Make room if needed
        if(this.buffer.size() == this.capacity)
            writeToDisk(this.buffer.remove( this.capacity - 1 ));

        // Push list
        this.buffer.add(0, page);
    }

    public void fullWrite(TableFile writeFile, int pageNumber, byte[] data) throws IOException {
        Page page = new Page(writeFile, this.pageSize, pageNumber, data);
        writeToBuffer(page);
        writeToDisk(page);
    }

    /**
     * Read page from the Page Buffer
     *
     * @param tableID Table ID to read from
     * @param pageNumber Page number to read from
     * @return page
     */
    public Page readFromBuffer(int tableID, int pageNumber, boolean removeFromBuffer) throws IOException {

        Page page = searchBuffer(tableID, pageNumber);     // assume not in buffer

        // Read page from disk if not in buffer
        // set to first to reduce search time
        if( page == null ){
            readFromDisk(tableID, pageNumber);
            page = searchBuffer(tableID, pageNumber);
        }

        // Push to top of buffer
        this.buffer.remove(page);
        if(!removeFromBuffer)
            this.buffer.add(0, page);

        return page;
    }



    /**
     * Write entire buffer to disk
     */
    public void flush() throws IOException {
        while(!this.buffer.isEmpty())
            writeToDisk(this.buffer.remove(0));
    }

}
