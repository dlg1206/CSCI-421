package cli.cmd.commands;

import java.util.Set;

import catalog.ICatalog;

import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;

import sm.StorageManager;

/**
 * <b>File:</b> CreateTable.java
 * <p>
 * <b>Description: Command to create a new table in the database</b>
 *
 * @author Derek Garcia
 */
public class CreateTable extends Command {

    private ICatalog catalog;

    public CreateTable(String args) throws InvalidUsage {
        // Create Table Syntax Validation
        String errorMessage = "Correct Usage: create table <table_name> (" +
                            "<column_name> <data_type> [constraints], ...);";
        if(!args.contains("(")){
            throw new InvalidUsage(args, errorMessage);
        }
        int indexOfOpen = args.indexOf("(");
        String firstPart = args.substring(0, indexOfOpen);
        String secondPart = args.substring(indexOfOpen + 1);
        
        String[] command = firstPart.split(" ");
        if (!command[1].equalsIgnoreCase("table") || command.length < 3) {
            throw new InvalidUsage(args, errorMessage);
        }

        if(!secondPart.contains(")")){
            throw new InvalidUsage(args, errorMessage);
        }
        String columns[] = secondPart.strip().substring(0, secondPart.length()-2).split(",");
        if(columns.length < 1){
            throw new InvalidUsage(args, errorMessage);
        }

        for (String col : columns) {
            if(col.strip().split(" ").length < 3){
                throw new InvalidUsage(args, errorMessage);
            }
        }

        // Create Table Semantical Validation
        String [] tempLst = args.toLowerCase().split("table");
        String tableName = tempLst[1].substring(0, tempLst[1].indexOf("("));

        Set<String> allTables = catalog.getExistingTableNames();
        if(!allTables.contains(tableName)){
            throw new InvalidUsage(args, "Table " + tableName + " Already Exists");
        }
    }

    @Override
    protected void helpMessage() {
        // TODO
    }

    @Override
    public void execute() throws ExecutionFailure {
        // TODO
    }
}
