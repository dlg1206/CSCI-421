package sm;

import catalog.Attribute;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * <b>File:</b> DatabaseFile.java
 * <p>
 * <b>Description:</b> Utility file to store metadata about the database file
 *
 * @author Derek Garcia
 */
class TableFile {

//    public record TableFileFactory(String databaseRoot) {
//        public TableFile createTableFile(){
//
//        }
//
//        public TableFile createSwapFile(){
//
//        }
//    }


    private static final String FILE_EXTENSION = ".db";
    private static final String SWAP_FILE_EXTENSION = ".swp.db";

    private final String databaseRoot;
    private final int tableID;
    private final String filePath;



    public TableFile(String databaseRoot, int tableID) throws IOException {
        this.databaseRoot = databaseRoot;
        this.tableID = tableID;
        this.filePath = this.databaseRoot + "/" + this.tableID + FILE_EXTENSION;

        // Init new table file if it DNE
        File tableFile = toFile();
        if(tableFile.createNewFile()){
            try (OutputStream os = new FileOutputStream(tableFile)) {
                os.write(0);
            }
        }
    }

    private TableFile(String databaseRoot, int tableID, String fileExtension) throws IOException {
        this.databaseRoot = databaseRoot;
        this.tableID = tableID;
        this.filePath = this.databaseRoot + "/" + this.tableID + fileExtension;

        // Init new table file if it DNE
        File tableFile = toFile();
        if(tableFile.createNewFile()){
            try (OutputStream os = new FileOutputStream(tableFile)) {
                os.write(0);
            }
        }

    }


    public TableFile getSwapFile() throws IOException {
        return new TableFile(this.databaseRoot, this.tableID, SWAP_FILE_EXTENSION);
    }


    public File toFile(){
        return new File(this.filePath);
    }

    public RandomAccessFile toRandomAccessFile() throws FileNotFoundException {
        return new RandomAccessFile(this.filePath, "rw");
    }




    /**
     * Delete old table file and create new one with swap file contents
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
     * Read the first byte of the database file to get the page count
     *
     * @return Number of pages
     * @throws IOException Failed to read file
     */
    public int readPageCount() throws IOException {

        try( RandomAccessFile raf = toRandomAccessFile() ){
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
    public void splitPage(PageBuffer buffer, int splitPageNum, List<Attribute> attributes) throws IOException {
        int swapOffset = 0;

        // Read each page from the original table file to the swap file
        for (int pageNumber = 0; pageNumber < this.readPageCount(); pageNumber++) {
            Page page = buffer.readFromBuffer(this.tableID, pageNumber, true);
            // add split page
            if (pageNumber == splitPageNum){
                Page rightPage = page.split(attributes);
                buffer.writeToBuffer(page.getSwapPage(swapOffset));
                swapOffset = 1;
                buffer.writeToBuffer(rightPage.getSwapPage(swapOffset));
            } else {
                // add rest of page
                buffer.writeToBuffer(page.getSwapPage(swapOffset));
            }
        }
        buffer.flush();     // Write out any remaining files
        closeSwapFile();
    }

    public boolean delete() {
        return toFile().delete();
    }

    public int getTableID() {
        return this.tableID;
    }

    public boolean isSwap(){
        return this.filePath.contains(SWAP_FILE_EXTENSION);
    }

    @Override
    public String toString() {
        return this.filePath;
    }
}
