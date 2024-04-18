package sm;

import java.io.*;

/**
 * <b>DBFile:</b> DBFile.java
 * <p>
 * <b>Description:</b> Generic file to use for database operations
 *
 * @author Derek Garcia
 */
abstract class DBFile {

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

    @Override
    public String toString() {
        return this.filePath;
    }
}
