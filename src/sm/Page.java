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
public class Page {
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

    public void insertRecord(int primaryKeyIndex, List<DataType> record){

        for(List<DataType> storedRecord : this.records){
            // -1, s < toAdd. 0, s = r, 1 s > r
            if(record.get(primaryKeyIndex).compareTo(storedRecord.get(primaryKeyIndex)) > 0){
                this.records.add(
                        this.records.indexOf(storedRecord),
                        record
                );
                return;
            }
        }
        // append to end
        this.records.add(record);
    }





    public int getTableID() {
        return this.tableID;
    }

    public int getNumber() {
        return this.number;
    }


}