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
class DatabaseFile{
    private static final String FILE_EXTENSION = ".db";
    private final String filePath;
    private final int tableID;

    /**
     * Create a new Database file if DNE or load from existing file
     *
     * @param databasePath root path of database files
     * @param tableID ID of table
     * @throws IOException Fails to write to file
     */
    public DatabaseFile(String databasePath, int tableID) throws IOException {
        this.filePath = databasePath + "/" + tableID + FILE_EXTENSION;
        this.tableID = tableID;

        // create file if DNE
        File tableFile = new File(this.filePath);
        if( !tableFile.exists() )
            Files.write(tableFile.toPath(), new byte[0]);

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
     * @return Full file path
     */
    public String getFilePath(){
        return this.filePath;
    }

    /**
     * @return Table ID
     */
    public int getTableID(){
        return this.tableID;
    }

}
