package cli.cmd.commands;

import java.io.IOException;
import java.util.List;
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
 * @author Derek Garcia, Clinten Hopkins
 */
public class DropTable extends Command {

    private final ICatalog catalog;
    private final StorageManager sm;

    private final String tableName;

    public DropTable(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {

        this.catalog = catalog;
        this.sm = storageManager;

        // Drop Table Syntax Validation
        List<String> input = getInput(args);
        if (!args.toLowerCase().contains("table") || input.size() != 3) {
            throw new InvalidUsage(args, "Correct Usage: (drop table <table>;)");
        }
        // Display Info Semantical Validation
        tableName = input.get(2);
        Set<String> allTables = catalog.getExistingTableNames();
        if(!allTables.contains(tableName)){
            throw new InvalidUsage(args, "Table " + tableName + " does not Exist in the Catalog");
        }
    }

    @Override
    protected void helpMessage() {
        // TODO
    }

    @Override
    public void execute() throws ExecutionFailure {
        catalog.deleteTable(tableName);
        int tableNumber = catalog.getTableNumber(tableName);
        try {
            sm.deleteTable(tableNumber);
        } catch (IOException ioe) {
            throw new ExecutionFailure("The file for table '%s' could not be opened or deleted.".formatted(tableName));
        }
    }
}
