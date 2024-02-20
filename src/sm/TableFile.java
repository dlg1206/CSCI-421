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
    private String fileExtension;      // default extension

    /**
     * Create a new Database file if DNE or load from existing file
     *
     * @param databasePath root path of database files
     * @param tableID ID of table
     * @throws IOException Fails to write to file
     */
    public TableFile(String databasePath, int tableID) throws IOException {
        new TableFile(databasePath, tableID, FILE_EXTENSION);


    }

    /**
     * Create Table file with custom path
     *
     * @param databasePath root path of database files
     * @param tableID ID of table
     * @param fileExtension either .db or .swp.db
     * @throws IOException Fails to write to file
     */
    private TableFile(String databasePath, int tableID, String fileExtension) throws IOException {
        this.databasePath = databasePath;
        this.tableID = tableID;
        this.fileExtension = fileExtension;

        // create file if DNE
        File tableFile = new File(buildFilePath());
        if( !tableFile.exists() )
            Files.write(tableFile.toPath(), new byte[0]);
    }

    /**
     * @return Full file path to table file
     */
    private String buildFilePath(){
        return this.databasePath + "/" + this.tableID + this.fileExtension;
    }

    /**
     * Read the first byte of the database file to get the page count
     *
     * @return Number of pages
     * @throws IOException Failed to read file
     */
    public int getPageCount() throws IOException {
        try (InputStream is = new FileInputStream(buildFilePath())) {
            byte[] result = new byte[1];
            return is.read(result, 0, 0);
        }
    }

    /**
     * Create a swap copy of the table file
     *
     * @return swap copy of the table file
     * @throws IOException Failed to read file
     */
    public TableFile createSwapFile() throws IOException {
        return new TableFile(this.databasePath, this.tableID, SWAP_FILE_EXTENSION);
    }


    /**
     * @return Table ID
     */
    public int getTableID(){
        return this.tableID;
    }
}
