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
    private int tableID;
    private int number;
    private List<List<DataType>> records = new ArrayList<>();

    /**
     * Create new Page
     *
     * @param tableID Table ID of the page
     * @param number  Page Number
     */
    public Page(int tableID, int number) {
        this.tableID = tableID;
        this.number = number;
    }

    public void addRecord(List<DataType> record){
        this.records.add(record);
    }

    public void addRecords(List<List<DataType>> records){
        this.records.addAll(records);
    }





    public int getTableID() {
        return this.tableID;
    }

    public int getNumber() {
        return this.number;
    }


}