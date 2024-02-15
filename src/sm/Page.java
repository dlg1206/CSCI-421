package sm;

/**
 * <b>File:</b> Page.java
 * <p>
 * <b>Description:</b> Utility Page object to hold metadata about page
 *
 * @author Derek Garcia
 */

import dataTypes.DataType;

import java.util.List;

/**
 * Create new Page
 *
 * @param tableID Table ID of the page
 * @param number Page Number
 * @param data Raw binary data of the page
 */
record Page(int tableID, int number, Byte[] data){
    private int getNumberOfRecords(){
        // parse data byte array to get number of records
        return -1;
    }

    private Byte[] getRecordBytes(){
        // truncate # records from data byte array
        return null;
    }

    public List<List<DataType>> getAllRecords(){
        // parse bytes into records and return
        return null;
    }
}