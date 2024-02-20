package sm;

import dataTypes.DataType;

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
    private final int maxCapacity;
    private int tableID;
    private int pageNumber;
    private final List<List<DataType>> records = new ArrayList<>();

    /**
     * Create new Page
     *
     * @param tableID Table ID of the page
     * @param pageNum  Page Number
     * @param maxCapacity Max number of records page can hold
     */
    public Page(int maxCapacity, int tableID, int pageNum) {
        this.maxCapacity = maxCapacity;
        this.tableID = tableID;
        this.pageNumber = pageNum;
    }

    /**
     * Insert a record to the page
     *
     * @param primaryKeyIndex Index of the primary key to sort by
     * @param record record to insert
     * @return True if inserted, false otherwise
     */
    public boolean insertRecord(int primaryKeyIndex, List<DataType> record){

        // Ordered insert
        for(List<DataType> storedRecord : this.records){
            // > 0 means record is less than stored
            if(record.get(primaryKeyIndex).compareTo(storedRecord.get(primaryKeyIndex)) > 0){
                this.records.add( this.records.indexOf(storedRecord),record );
                return true;
            }
        }

        // Append if there's space
        if(this.records.size() < this.maxCapacity){
            appendRecord(record);
            return true;
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
    public void appendRecord(List<DataType> record){
        this.records.add(record);
    }

    /**
     * Check if the page is above capacity
     *
     * @return true if overfull, false otherwise
     */
    public boolean isOverfull(){
        return this.records.size() > this.maxCapacity;
    }


    /**
     * Split the current page into 2. The first half will remain in the current page
     *
     * @return the second half of the page
     */
    public Page split(){
        // Create second page
        Page newPage = new Page(this.maxCapacity, this.tableID, this.pageNumber + 1);
        int mid = this.records.size() / 2;
        for( int i = mid; i < this.records.size(); i++)
            newPage.appendRecord(this.records.get(i));

        // Remove second page from this page
        this.records.subList(mid, this.records.size()).clear();

        return newPage;
    }

    /**
     * Mark this page to be written to a swap file
     */
    public void markSwap(){
        this.tableID = -Math.abs(this.tableID);
    }


    /**
     * Set the page number
     * @param pageNumber new page number
     */
    public void setPageNumber(int pageNumber){
        this.pageNumber = pageNumber;
    }


    /**
     * @return Table ID
     */
    public int getTableID() {
        return this.tableID;
    }

    /**
     * @return Page Number
     */
    public int getPageNumber() {
        return this.pageNumber;
    }
}