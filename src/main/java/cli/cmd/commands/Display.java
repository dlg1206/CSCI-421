package cli.cmd.commands;

import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import catalog.Attribute;
import catalog.ICatalog;
import catalog.Table;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import dataTypes.AttributeType;
import util.Console;
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
    private String tableName;

    private static final Pattern FULL_MATCH = Pattern.compile("display[\\s\\t]+(?:schema|info[\\s\\t]+([a-z0-9]+))[\\s\\t]*;", Pattern.CASE_INSENSITIVE);
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("[a-z][a-z0-9]*", Pattern.CASE_INSENSITIVE);


    public Display(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {

        this.catalog = catalog;
        this.sm = storageManager;

        Matcher fullMatcher = FULL_MATCH.matcher(args);

        if (!fullMatcher.matches()) {
            throw new InvalidUsage(args, """
                    Correct Usage: display info <table>;
                     display schema;""");
        }

        tableName = fullMatcher.group(1);

        if (tableName != null) {
            Matcher tableNameMatcher = TABLE_NAME_PATTERN.matcher(tableName);
            if (!tableNameMatcher.matches()) {
                throw new InvalidUsage(args, "The name '%s' is not a valid table name.".formatted(tableName));
            }

            tableName = tableName.toLowerCase();

            Set<String> allTables = catalog.getExistingTableNames();
            if(!allTables.contains(tableName)){
                throw new InvalidUsage(args, "Table " + tableName + " does not Exist in the Catalog \nERROR");
            }
        }
    }

    @Override
    protected void helpMessage() {
        // TODO
    }

    @Override
    public void execute() throws ExecutionFailure { 
        if (tableName != null) {
            Table tableSchema = catalog.getRecordSchema(tableName);
            int tableID = tableSchema.getNumber();
            int pageCount = sm.getPageCount(tableID);
            int recordCount = sm.getAllRecords(tableID, tableSchema.getAttributes()).size();
            printTable(tableSchema);
            Console.out("Pages: " + pageCount);
            Console.out("Records: " + recordCount);
            Console.out("SUCCESS");
        }
        else{
            Set<String> allTableNames = catalog.getExistingTableNames();
            int bufferSize = sm.getBufferSize();
            int pageSize = sm.getPageSize();
            String location = sm.getDatabaseRoot();
            Console.out("DB location: " + location);
            Console.out("Page Size: " + pageSize);
            Console.out("Buffer Size: " + bufferSize);
            if(allTableNames.isEmpty()){
                Console.out("\nNo tables to display");
            }
            else{
                Console.out("Tables: \n");
            }
            for (String name : allTableNames) {
                Table tempTable = catalog.getRecordSchema(name);
                int tableID = tempTable.getNumber();
                int pageCount = sm.getPageCount(tableID);
                int recordCount = sm.getAllRecords(tableID, tempTable.getAttributes()).size();
                printTable(tempTable);
                Console.out("Pages: " + pageCount);
                Console.out("Records: " + recordCount);
                Console.out("\n");
            }
            Console.out("SUCCESS");
        }
    }

    public void printTable(Table table){
        List<Attribute> attributes = table.getAttributes();
        Console.out("Table Name: " + table.getName());
        Console.out("Table Schema: ");
        for (Attribute attr : attributes) {
            String temp = "     " + attr.getName() + ":" + attr.getDataType().name().toLowerCase();
            if (attr.getDataType() == AttributeType.CHAR || attr.getDataType() == AttributeType.VARCHAR) {
                temp += "(%s)".formatted(attr.getMaxDataLength());
            }
            if(attr.isPrimaryKey()){
                temp = temp + " primarykey";
            }
            else{
                if(!attr.isNullable()){
                    temp = temp + " notnull";
                }
                if(attr.isUnique()){
                    temp = temp + " unique";
                }
            }
            Console.out(temp);
            }
    }
    
}
