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
 * <b>Description:</b> Command to create a new table in the database
 *
 * @author Derek Garcia, Clinten Hopkins
 */
public class DropTable extends Command {

    private final ICatalog catalog;
    private final StorageManager sm;

    private final String tableName;

    /**
     * Create a new Drop Table command to be executed. Parse the arguments to allow
     * {@link DropTable#execute() execute} to operate.
     *
     * @param args The string representation of the command passed to the CLI.
     * @param catalog The catalog of the current DB.
     * @param storageManager The storage manager of the current DB.
     * @throws InvalidUsage when the arguments could not be parsed.
     */
    public DropTable(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {

        this.catalog = catalog;
        this.sm = storageManager;

        // Drop Table Syntax Validation
        List<String> input = getInput(args);
        if (!args.toLowerCase().contains("table") || input.size() != 3) {
            throw new InvalidUsage(args, "Correct Usage: (drop table <table>;)");
        }
        // Display Info Semantic Validation
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

    /**
     * Delete a table from the catalog, flush everything out of the page buffer, then delete the file.
     *
     * @throws ExecutionFailure when the table's file cannot be read or modified.
     */
    @Override
    public void execute() throws ExecutionFailure {
        catalog.deleteTable(tableName);
        int tableNumber = catalog.getTableNumber(tableName);
        try {
            sm.dropTable(tableNumber);
        } catch (IOException ioe) {
            throw new ExecutionFailure("The file for table '%s' could not be opened or deleted.".formatted(tableName));
        }
    }
}
