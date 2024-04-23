package sm;

import catalog.Attribute;
import dataTypes.DataType;
import util.BPlusTree.RecordPointer;

import java.io.*;
import java.util.List;

/**
 * <b>File:</b> DatabaseFile.java
 * <p>
 * <b>Description:</b> Utility file to store metadata about the database file
 *
 * @author Derek Garcia
 */
class TableFile {


    private static final String FILE_EXTENSION = ".db";
    private static final String SWAP_FILE_EXTENSION = ".swp.db";

    private final String databaseRoot;
    private final int tableID;
    private final String filePath;


    /**
     * Create a new table file
     *
     * @param databaseRoot Root path of the database
     * @param tableID      Table ID of this file
     * @throws IOException Failed to create or open file
     */
    public TableFile(String databaseRoot, int tableID) throws IOException {
        this.databaseRoot = databaseRoot;
        this.tableID = tableID;
        this.filePath = this.databaseRoot + "/" + this.tableID + FILE_EXTENSION;

        // Init new table file if it DNE
        File tableFile = toFile();
        if (tableFile.createNewFile()) {
            try (OutputStream os = new FileOutputStream(tableFile)) {
                os.write(0);
            }
        }
    }

    /**
     * Private constructor used for swap file creation
     *
     * @param databaseRoot  Root path of the database
     * @param tableID       Table ID of this file
     * @param fileExtension File extension to append to file
     * @throws IOException Failed to create or open file
     */
    private TableFile(String databaseRoot, int tableID, String fileExtension) throws IOException {
        this.databaseRoot = databaseRoot;
        this.tableID = tableID;
        this.filePath = this.databaseRoot + "/" + this.tableID + fileExtension;

        // Init new table file if it DNE
        File tableFile = toFile();
        if (tableFile.createNewFile()) {
            try (OutputStream os = new FileOutputStream(tableFile)) {
                os.write(0);
            }
        }
    }


    /**
     * @return This as File object
     */
    public File toFile() {
        return new File(this.filePath);
    }

    /**
     * @return This as RandomAccessFile
     * @throws FileNotFoundException Table file does not exist
     */
    public RandomAccessFile toRandomAccessFile() throws FileNotFoundException {
        return new RandomAccessFile(this.filePath, "rw");
    }


    /**
     * Read the first byte of the database file to get the page count
     *
     * @return Number of pages
     * @throws IOException Failed to read file
     */
    public int readPageCount() throws IOException {
        try (RandomAccessFile raf = toRandomAccessFile()) {
            byte[] buffer = new byte[1];
            raf.read(buffer, 0, 1);
            return buffer[0];
        }
    }

    /**
     * Delete last page and update page count
     *
     * @throws IOException Failed to read file
     */
    private void deleteLastPageFromFile(int pageSize) throws IOException {
        try (RandomAccessFile raf = toRandomAccessFile()) {
            // update page count
            byte[] buffer = new byte[]{(byte) (readPageCount() - 1)};
            raf.write(buffer, 0, 1);

            // remove last page from file
            raf.setLength(1 + (long) readPageCount() * pageSize);
        }
    }

    /**
     * Insert a split page into the table file
     *
     * @param buffer       Page buffer to use to iterate through pages
     * @param splitPageNum Page index to split on
     * @param attributes   Constants of data types
     * @param p            Page to split
     * @param record       Record that has been inserted
     * @return Record pointer to the split page containing the given record
     * @throws IOException Failed tor read from file
     */
    public RecordPointer splitPage(PageBuffer buffer, int splitPageNum, List<Attribute> attributes, Page p, List<DataType> record) throws IOException {
        int swapOffset = 0;
        int pageCount = readPageCount();
        RecordPointer recordPointer = null;

        // Read each page from the original table file to the swap file
        for (int pageNumber = 0; pageNumber < pageCount; pageNumber++) {
            Page page = buffer.readFromBuffer(this.tableID, pageNumber, true);
            // add split page
            if (pageNumber == splitPageNum) {
                // Get left and right swap pages
                Page rightPage = p.split(attributes);
                SwapPage leftSwapPage = p.getSwapPage(swapOffset);
                swapOffset = 1;
                SwapPage rightSwapPage = rightPage.getSwapPage(swapOffset);

                buffer.writeToBuffer(leftSwapPage);     // add leftPage
                buffer.writeToBuffer(rightSwapPage);    // add rightPage

                // If left swap  has record, point to left swap else point to right swap
                recordPointer = leftSwapPage.indexOf(attributes, record) != -1
                        ? new RecordPointer(leftSwapPage.getPageNumber(), leftSwapPage.indexOf(attributes, record))
                        : new RecordPointer(rightSwapPage.getPageNumber(), rightSwapPage.indexOf(attributes, record));

            } else {
                // add rest of page
                buffer.writeToBuffer(page.getSwapPage(swapOffset));
            }
        }
        buffer.flush();     // Write out any remaining files
        closeSwapFile();    // Save the swap file as the actual file
        return recordPointer;
    }

    /**
     * Delete a page from the table file
     *
     * @throws IOException Failed to read file
     */
    public void deletePage(PageBuffer buffer, int emptyPageNum) throws IOException {
        int pageCount = readPageCount();

        // get page size and remove empty page from buffer
        int pageSize = buffer.readFromBuffer(tableID, emptyPageNum, true).getPageSize();

        // move all pages after empty page forward
        for (int pageNumber = emptyPageNum + 1; pageNumber < pageCount; pageNumber++) {
            Page page = buffer.readFromBuffer(this.tableID, pageNumber, true);
            //buffer.writeToBuffer(page.getSwapPage(swapOffset));
            buffer.writeToBuffer(new Page(this, pageSize, pageNumber - 1, page.getData()));
        }

        // Write out any remaining files
        buffer.flush();

        deleteLastPageFromFile(pageSize);
    }

    /**
     * Delete this table file
     *
     * @return true if delete, false otherwise
     */
    public boolean delete() {
        return toFile().delete();
    }

    /**
     * Delete old table file and save new one with swap file contents
     *
     * @throws IOException Fails to write to file
     */
    private void closeSwapFile() throws IOException {
        delete();
        TableFile swapFile = getSwapFile();
        swapFile.toFile().renameTo(toFile());
        swapFile.delete();
    }

    /**
     * Get the swap file for this table file
     *
     * @return Swap Table file
     * @throws IOException Failed to create swap table file
     */
    public TableFile getSwapFile() throws IOException {
        return new TableFile(this.databaseRoot, this.tableID, SWAP_FILE_EXTENSION);
    }

    /**
     * @return True if swap file, false otherwise
     */
    public boolean isSwap() {
        return this.filePath.contains(SWAP_FILE_EXTENSION);
    }

    /**
     * @return Table ID
     */
    public int getTableID() {
        return this.tableID;
    }


    @Override
    public String toString() {
        return this.filePath;
    }
}
