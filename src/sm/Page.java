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
    private int maxCapacity;
    private int tableID;
    private int pageNumber;

    private List<List<DataType>> records = new ArrayList<>();

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

    public void appendRecord(List<DataType> record){
        this.records.add(record);
    }

    public boolean isOverfull(){
        return this.records.size() > this.maxCapacity;
    }


    public Page split(int newPageNumber){
        Page newPage = new Page(this.maxCapacity, this.tableID, newPageNumber);
        int mid = this.records.size() / 2;

        for( int i = mid; i < this.records.size(); i++){
            newPage.appendRecord(this.records.get(i));
        }

        this.records.subList(mid, this.records.size()).clear();

        return newPage;

    }

    public void setPageNumber(int pageNumber){
        this.pageNumber = pageNumber;
    }

    public void markSwap(){
        this.tableID = -Math.abs(this.tableID);
    }


    public int getTableID() {
        return this.tableID;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }


}