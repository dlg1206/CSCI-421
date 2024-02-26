package sm;

import catalog.Attribute;

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
     * Insert a split page into the table file
     *
     * @throws IOException Failed to read file
     */
    public void splitPage(PageBuffer buffer, int splitPageNum, List<Attribute> attributes, Page p) throws IOException {
        int swapOffset = 0;
        int pageCount = readPageCount();

        // Read each page from the original table file to the swap file
        for (int pageNumber = 0; pageNumber < pageCount; pageNumber++) {
            Page page = buffer.readFromBuffer(this.tableID, pageNumber, true);
            // add split page
            if (pageNumber == splitPageNum) {
                Page rightPage = p.split(attributes);
                buffer.writeToBuffer(p.getSwapPage(swapOffset));         // add leftPage
                swapOffset = 1;
                buffer.writeToBuffer(rightPage.getSwapPage(swapOffset));    // add rightPage
            } else {
                // add rest of page
                buffer.writeToBuffer(page.getSwapPage(swapOffset));
            }
        }
        buffer.flush();     // Write out any remaining files
        closeSwapFile();    // Save the swap file as the actual file
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
