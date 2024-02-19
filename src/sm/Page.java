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
    private int number;

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
        this.number = pageNum;
    }

    public boolean insertRecord(int primaryKeyIndex, List<DataType> record){
        for(List<DataType> storedRecord : this.records){
            // > 0 means record is less than stored
            if(record.get(primaryKeyIndex).compareTo(storedRecord.get(primaryKeyIndex)) > 0){
                this.records.add( this.records.indexOf(storedRecord),record );
                return true;
            }
        }

        // append to end if there's space
        if(this.records.size() <= this.maxCapacity){
            this.records.add(record);
            return true;
        }

        // Record wasn't added
        return false;

    }





    public int getTableID() {
        return this.tableID;
    }

    public int getNumber() {
        return this.number;
    }


}