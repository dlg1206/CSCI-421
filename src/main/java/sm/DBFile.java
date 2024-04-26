package sm;

import java.io.*;

/**
 * <b>File:</b> DBFile.java
 * <p>
 * <b>Description:</b> Generic file to use for database operations
 *
 * @author Derek Garcia
 */
public abstract class DBFile {

    static final String DB_FILE_EXTENSION = "db";
    static final String DB_SWAP_FILE_EXTENSION = "swp.db";
    static final String INDEX_FILE_EXTENSION = "idx";

    protected final String databaseRoot;
    protected final int fileID;
    protected final String filePath;


    /**
     * Create a new file
     *
     * @param databaseRoot Root path of the database
     * @param fileID       ID of this file
     * @param extension    File extension to use
     * @throws IOException Failed to create or open file
     */
    public DBFile(String databaseRoot, int fileID, String extension) throws IOException {
        this.databaseRoot = databaseRoot;
        this.fileID = fileID;
        this.filePath = "%s/%s.%s".formatted(this.databaseRoot, this.fileID, extension);

        // Init new file if it DNE
        File file = toFile();
        if (file.createNewFile()) {
            try (OutputStream os = new FileOutputStream(file)) {
                os.write(0);
            }
        }
    }


    /**
     * @return This as DBFile object
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
     * Delete this file
     *
     * @return true if delete, false otherwise
     */
    public boolean delete() {
        return toFile().delete();
    }

    /**
     * @return True if swap file, false otherwise
     */
    public boolean isSwap() {
        return this.filePath.contains(DB_SWAP_FILE_EXTENSION);
    }

    /**
     * @return True if index file, false otherwise
     */
    public boolean isIndex() {
        return this.filePath.contains(INDEX_FILE_EXTENSION);
    }

    @Override
    public String toString() {
        return this.filePath;
    }

    public abstract int getTableID();

    /**
     * Get the swap file for this table file
     *
     * @return Swap Table file
     * @throws IOException Failed to create swap table file
     */
    public abstract TableFile getSwapFile() throws IOException;
}
