package cli.cmd.commands;

import java.util.Set;

import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import catalog.ICatalog;
import sm.StorageManager;

/**
 * <b>File:</b> DropTable.java
 * <p>
 * <b>Description: Command to create a new table in the database</b>
 *
 * @author Derek Garcia
 */
public class DropTable extends Command {

    private ICatalog catalog;

    public DropTable(String args) throws InvalidUsage {
        // Drop Table Syntax Validation
        String[] input = args.strip().split(" ");
        if (!args.toLowerCase().contains("table") || input.length != 3) {
            throw new InvalidUsage(args, "Correct Usage: (drop table <table>;)");
        }
        // Display Info Semantical Validation
        String table = input[2];
        Set<String> allTables = catalog.getExistingTableNames();
        if(!allTables.contains(table)){
            throw new InvalidUsage(args, "Table " + table + " does not Exist in the Catalog");
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
