package cli.cmd.commands;

import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import dataTypes.AttributeType;
import catalog.Attribute;
import catalog.ICatalog;
import catalog.Table;

import java.util.List;
import java.util.Set;
import sm.StorageManager;

/**
 * <b>File:</b> Display.java
 * <p>
 * <b>Description: Command to display information about the database</b>
 *
 * @author Derek Garcia
 */
public class Display extends Command {

    private final ICatalog catalog;
    private final StorageManager sm;
    private final String args;

    private final String tableName;

    public Display(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {

        this.catalog = catalog;
        this.sm = storageManager;
        this.args = args;

        // Display Info Syntax Validation
        List<String> input = getInput(args);
        System.out.println(input);
        if (args.toLowerCase().contains("info")) {
            if (input.size() != 3) {
                throw new InvalidUsage(args, "Correct Usage: display info <table>;");
            }
        } else if (args.toLowerCase().contains("schema")) { 
            if (input.size() != 2) {
                throw new InvalidUsage(args, "Correct Usage: display schema;");
            }
        } else {
            throw new InvalidUsage(args, "Correct Usage: display info <table>; display schema;");
        }
   
        // Display Info Semantical Validation
        if (args.toLowerCase().contains("info")) {
            tableName = input.get(2);
            Set<String> allTables = catalog.getExistingTableNames();
            if(!allTables.contains(tableName)){
                throw new InvalidUsage(args, "Table " + tableName + " does not Exist in the Catalog");
            }
        }
        else{
            tableName = "";
        }


    }

    @Override
    protected void helpMessage() {
        // TODO
    }

    @Override
    public void execute() throws ExecutionFailure { 
        if (args.toLowerCase().contains("info")) {
            Table tableSchema = catalog.getRecordSchema(tableName);
            int tableID = tableSchema.getNumber();
            int pageCount = sm.getPageCount(tableID);
            int recordCount = sm.getCountOfRecords(tableID);
            printTable(tableSchema);
            System.out.println("Pages: " + pageCount);
            System.out.println("Records: " + recordCount);
            System.out.println("SUCCESS");
        }
        else{
            Set<String> allTableNames = catalog.getExistingTableNames();
            int bufferSize = sm.getBufferSize();
            int pageSize = sm.getPageSize();
            String location = sm.getDatabaseRoot();
            System.out.println("DB location: " + location);
            System.out.println("Page Size: " + pageSize);
            System.out.println("Buffer Size: " + bufferSize);
            System.out.println("\nTables: ");
            for (String name : allTableNames) {
                Table tempTable = catalog.getRecordSchema(name);
                int tableID = tempTable.getNumber();
                int pageCount = sm.getPageCount(tableID);
                int recordCount = sm.getCountOfRecords(tableID);
                System.out.println("\n");
                printTable(tempTable);
                System.out.println("Pages: " + pageCount);
                System.out.println("Records: " + recordCount);
                

            }
            System.out.println("SUCCESS");
        }
    }

    public void printTable(Table table){
        List<Attribute> attributes = table.getAttributes();
        System.out.println("Table Name: " + table.getName());
        System.out.println("Table Schema: ");
        for (Attribute attr : attributes) {
            String temp = "\n" + attr.getName() + ":" + getStringType(attr.getDataType());
            if(attr.isPrimaryKey()){
                temp = temp + " primarykey";
            }
            if(!attr.isNullable()){
                temp = temp + " notnull";
            }
            if(attr.isUnique()){
                temp = temp + " unique";
            }
            System.out.println(temp);
            }
    }

    public String getStringType(AttributeType attr) {
        switch (attr) {
            case INTEGER -> {
                return "integer";
            }
            case DOUBLE -> {
                return "double";
            }
            case BOOLEAN -> {
                return "boolean";
            }
            case CHAR -> {
                return "char";
            }
            default -> {
                return "varchar";
            }
        }
    }
    
}
