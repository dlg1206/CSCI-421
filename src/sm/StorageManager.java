package sm;

import sm.tmp.DataType;

import java.util.List;

/**
 * <b>File:</b> StorageManager.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class StorageManager {



     private int getPageCount(int tableID){
         return 1;
     }

     private byte[] getPage(int tableID, int pageNum){
         return null;
     }

    // CREATE
    public void insertRecord(int tableID, DataType primaryKey, List<DataType> record){

    }

     // READ
     public List<DataType> getRecord(int tableID, DataType primaryKey){
         return null;
     }

     // READ
    public List<List<DataType>> getAllRecords(){
         return null;
    }


     // UPDATE
     public void updateRecord(int tableID, DataType primaryKey, List<DataType> record){

     }

    // DELETE
    public void deleteRecord(int tableID, DataType primaryKey){

    }







}
