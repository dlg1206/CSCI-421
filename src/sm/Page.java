package sm;

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
//    public boolean insertRecord(int primaryKeyIndex, List<DataType> record){
//
//        // Ordered insert
//        for(List<DataType> storedRecord : this.records){
//            // > 0 means record is less than stored
//            if(record.get(primaryKeyIndex).compareTo(storedRecord.get(primaryKeyIndex)) > 0){
//                this.records.add( this.records.indexOf(storedRecord),record );
//                return true;
//            }
//        }
//
//        // Append if there's space
//        if(this.records.size() < this.pageSize){
//            appendRecord(record);
//            return true;
//        }
//
//        // Record wasn't added
//        return false;
//    }

    /**
     * Append record to end of page
     * SHOULD ONLY BE USED IF LAST PAGE
     *
     * @param record record to append
     */
//    public void appendRecord(List<DataType> record){
//        this.records.add(record);
//    }

    /**
     * Check if the page is above capacity
     *
     * @return true if overfull, false otherwise
     */
//    public boolean isOverfull(){
//        return this.records.size() > this.pageSize;
//    }


    /**
     * Split the current page into 2. The first half will remain in the current page
     *
     * @return the second half of the page
     */
//    public Page split(){
//        // Create second page
//        Page newPage = new Page(this.pageSize, this.tableID, this.pageNumber + 1);
//        int mid = this.records.size() / 2;
//        for( int i = mid; i < this.records.size(); i++)
//            newPage.appendRecord(this.records.get(i));
//
//        // Remove second page from this page
//        this.records.subList(mid, this.records.size()).clear();
//
//        return newPage;
//    }

    /**
     * Mark this page to be written to a swap file
     */
//    public void markSwap(){
//        this.tableID = -Math.abs(this.tableID);
//    }


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