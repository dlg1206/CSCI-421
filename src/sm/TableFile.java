package sm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

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
    private String databasePath;
    private int tableID;
    private String filePath;
    private String swapFilePath;

    /**
     * Create a new Database file if DNE or load from existing file
     *
     * @param databasePath root path of database files
     * @param tableID      ID of table
     * @throws IOException Fails to write to file
     */
    public TableFile(String databasePath, int tableID) throws IOException {
        this.databasePath = databasePath;
        this.tableID = tableID;
        this.filePath = databasePath + "/" + this.tableID + FILE_EXTENSION;
        this.swapFilePath = databasePath + "/" + this.tableID + SWAP_FILE_EXTENSION;
        initFile(this.filePath);
    }

    private void initFile(String path) throws IOException {
        // create file if DNE
        File tableFile = new File(path);
        if (!tableFile.exists())
            Files.write(tableFile.toPath(), new byte[0]);
    }

    private void closeSwapFile() throws IOException {
        File tableFile = new File(this.filePath);
        File swapTableFile = new File(this.swapFilePath);

        Files.deleteIfExists(tableFile.toPath());
        swapTableFile.renameTo(tableFile);
        Files.deleteIfExists(swapTableFile.toPath());
    }


    /**
     * Read the first byte of the database file to get the page count
     *
     * @return Number of pages
     * @throws IOException Failed to read file
     */
    public int getPageCount() throws IOException {
        try (InputStream is = new FileInputStream(this.filePath)) {
            byte[] result = new byte[1];
            return is.read(result, 0, 0);
        }
    }

    /**
     * Insert a split page into the table file
     *
     * @throws IOException Failed to read file
     */
    public void splitPageInFile(PageBuffer buffer, int splitPageNum) throws IOException {
        initFile(this.swapFilePath);
        int swpPageNum = 0;

        // Read each page from the original table file to the swap file
        for (int pageNum = 0; pageNum < this.getPageCount(); pageNum++) {
            Page page = buffer.readFromBuffer(this.tableID, pageNum);
            page.markSwap();

            // add split page
            if (pageNum == splitPageNum)
                buffer.writeToBuffer(page.split(swpPageNum++));

            // add rest of page
            page.setPageNumber(swpPageNum++);
            buffer.writeToBuffer(page);
        }

        buffer.flush();     // Write out any remaining files
        closeSwapFile();    // Save the swap as the actual table file
    }
}
