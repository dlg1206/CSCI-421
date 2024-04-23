package sm;

import dataTypes.DataType;
import util.BPlusTree.RecordPointer;

import java.io.IOException;

/**
 * <b>DBFile:</b> IndexFile.java
 * <p>
 * <b>Description:</b> Utility file to store metadata about the index of a database file
 *
 * @author Derek Garcia
 */
class IndexFile extends DBFile{

    private static final String INDEX_FILE_EXTENSION = "idx";

    /**
     * Create a new Index file
     *
     * @param databaseRoot Root path of the database
     * @param tableID      table ID of the file this is an index of
     * @throws IOException Failed to create or open file
     */
    public IndexFile(String databaseRoot, int tableID) throws IOException {
        super(databaseRoot, tableID, INDEX_FILE_EXTENSION);
    }


    public void insertPointer(PageBuffer buffer, RecordPointer recordPointer){
        // TODO b+ tree logic?
    }

    public void deletePointer(DataType primaryKey){
        // TODO b+ delete pointer
    }
}
