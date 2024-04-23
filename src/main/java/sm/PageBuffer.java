package sm;

import java.io.IOException;
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
class PageBuffer {
    private final List<Page> buffer = new ArrayList<>();
    private final int capacity;
    private final int pageSize;
    private final String databaseRoot;


    /**
     * Create a new Page Buffer
     *
     * @param capacity Max buffer size in number of pages
     * @param pageSize Max page size in number of records
     */
    public PageBuffer(int capacity, int pageSize, String databaseRoot) {
        this.capacity = capacity;
        this.pageSize = pageSize;
        this.databaseRoot = databaseRoot;
    }


    /**
     * Search the page buffer for a given page
     *
     * @param tableID    Table ID of page
     * @param pageNumber Page number
     * @return Page if in buffer, null otherwise
     */
    private Page searchBuffer(int tableID, int pageNumber) {
        for (Page p : this.buffer) {
            // Find page
            if (p.match(tableID, pageNumber))
                return p;
        }
        return null;
    }


    /**
     * Write a page to disk
     *
     * @param page Page to write to disk
     * @throws IOException Failed to open table file
     */
    private void writeToDisk(Page page) throws IOException {
        DBFile writeFile = page.getWriteFile();
        try (RandomAccessFile raf = writeFile.toRandomAccessFile()) {
            // Write page data
            raf.seek(1 + (long) page.getPageNumber() * this.pageSize);  // +1 is page count byte
            raf.write(page.getData());
            // Update page count
            raf.seek(0);
            raf.write((int) ((raf.length() - 1) / this.pageSize));
        }
    }


    /**
     * Read Page binary from Table file from disk to buffer
     *
     * @param tableID    Table ID to read from
     * @param pageNumber Page number to get
     */
    private void readFromDisk(int tableID, int pageNumber, boolean isIndexFile) throws IOException {
        DBFile writeFile;
        if (!isIndexFile)
            writeFile = new TableFile(this.databaseRoot, tableID);
        else
            writeFile = new IndexFile(this.databaseRoot, tableID);

        byte[] buffer = new byte[this.pageSize];

        // Read page from file
        try (RandomAccessFile raf = writeFile.toRandomAccessFile()) {
            raf.seek(1 + (long) pageNumber * this.pageSize);  // +1 is page count byte
            raf.read(buffer, 0, this.pageSize);
        }

        writeToBuffer(new Page(writeFile, this.pageSize, pageNumber, buffer));
    }

    /**
     * Write page to buffer
     *
     * @param page Page to add to buffer
     */
    public void writeToBuffer(Page page) throws IOException {

        // Make room if needed
        if (this.buffer.size() == this.capacity)
            writeToDisk(this.buffer.remove(this.capacity - 1));

        // Push list
        this.buffer.addFirst(page);
    }


    /**
     * Read page from the Page Buffer
     *
     * @param tableID          Table ID to read from
     * @param pageNumber       Page number to read from
     * @param removeFromBuffer Remove this page from the buffer ( used for splitting pages )
     * @return Page
     */
    public Page readFromBuffer(int tableID, int pageNumber, boolean removeFromBuffer, boolean isIndexFile) throws IOException {

        Page page = searchBuffer(tableID, pageNumber);

        // Read page from disk if not in buffer
        // set to first to reduce search time
        if (page == null) {
            readFromDisk(tableID, pageNumber, isIndexFile);
            page = searchBuffer(tableID, pageNumber);
        }

        // Push to top of buffer
        this.buffer.remove(page);
        if (!removeFromBuffer)
            this.buffer.addFirst(page);

        return page;
    }

    /**
     * Immediately write the first page to hardware ( used for new table )
     *  todo remove or move?
     *
     * @param writeFile  File to write the page to
     * @param pageNumber Page number
     * @param data       binary data of page
     * @throws IOException Failed to write to file
     */
    public void fullWrite(TableFile writeFile, int pageNumber, byte[] data) throws IOException {
        Page page = new Page(writeFile, this.pageSize, pageNumber, data);
        writeToBuffer(page);
        writeToDisk(page);
    }

    /**
     * Pop and write each entry in the buffer to file
     */
    public void flush() throws IOException {
        while (!this.buffer.isEmpty())
            writeToDisk(this.buffer.removeFirst());
    }

}
