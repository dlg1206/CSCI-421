package sm;

import catalog.Attribute;
import dataTypes.DataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>File:</b> Page.java
 * <p>
 * <b>Description:</b> Utility Page object to hold metadata about page
 *
 * @author Derek Garcia, Ryan Nowak
 */
class Page {


//    public record PageFactory(int pageSize) {
//
//        public Page createCache(int tableID, int pageNum, byte[] data) {
//            return new Page(tableID, pageNum, this.pageSize, data);
//        }
//
//    }

    private final int pageSize;
    private TableFile writeFile;
    private int pageNumber;
    private byte[] data;
//    private final List<List<DataType>> records = new ArrayList<>();

    /**
     * Create new Empty Page
     *
     * @param tableID Table ID of the page
     * @param pageNumber  Page Number
     * @param pageSize Max number of records page can hold
     */
    public Page(TableFile writeFile, int pageSize, int pageNumber, byte[] data) {
        this.writeFile = writeFile;
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.data = new byte[pageSize];

        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    public boolean match(int tableID, int pageNumber){
        return this.writeFile.getTableID() == tableID
                && this.pageNumber == pageNumber
                && !this.writeFile.isSwap();    // cannot read from swap
    }

    /**
     * Insert a record to the page
     *
     * @param primaryKeyIndex Index of the primary key to sort by
     * @param record record to insert
     * @return True if inserted, false otherwise
     */
    public boolean insertRecord(int primaryKeyIndex, List<Attribute> attributes, List<DataType> record){

        List<List<DataType>> records = BInterpreter.convertPageToRecords(this.data, attributes);

        // Ordered insert
        for(List<DataType> storedRecord : records){
            // > 0 means record is less than stored
            if(record.get(primaryKeyIndex).compareTo(storedRecord.get(primaryKeyIndex)) > 0){
                records.add( records.indexOf(storedRecord), record );
                this.data = BInterpreter.convertRecordsToPage(records);
                return true;
            }
        }

        // Record wasn't added
        return false;
    }

    /**
     * Append record to end of page
     * SHOULD ONLY BE USED IF LAST PAGE
     *
     * @param record record to append
     */
    public void appendRecord(List<Attribute> attributes, List<DataType> record){
        List<List<DataType>> records = BInterpreter.convertPageToRecords(this.data, attributes);
        records.add(record);
        this.data = BInterpreter.convertRecordsToPage(records);
    }

    /**
     * Check if the page is above capacity
     *
     * @return true if overfull, false otherwise
     */
    public boolean isOverfull(){
        return this.data.length > this.pageSize;
    }


    /**
     * Split the current page into 2. The first half will remain in the current page
     *
     * @return the second half of the page
     */
    public Page split(List<Attribute> attributes) throws IOException {
        List<List<DataType>> leftRecords = BInterpreter.convertPageToRecords(this.data, attributes);

        // Split right from all records
        List<List<DataType>> rightRecords = new ArrayList<>(leftRecords.subList(leftRecords.size() / 2, leftRecords.size()));

        // Create second page
        Page rightPage = new SwapPage(
                this.writeFile,
                this.pageSize,
                this.pageNumber,
                BInterpreter.convertRecordsToPage(rightRecords)
        );

        // Remove right page from this page
        leftRecords.subList(leftRecords.size() / 2, leftRecords.size()).clear();
        this.data = BInterpreter.convertRecordsToPage(leftRecords);

        return rightPage;
    }

    public SwapPage getSwapPage(int offset) throws IOException {
        return new SwapPage(
                writeFile.getSwapFile(),
                this.pageSize,
                this.pageNumber + offset,
                this.data
        );
    }



    public byte[] getData() {
        return this.data;
    }

    /**
     * @return Page Number
     */
    public int getPageNumber() {
        return this.pageNumber;
    }

    public TableFile getWriteFile() {
        return this.writeFile;
    }
}