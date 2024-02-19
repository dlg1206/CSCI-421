package cli.cmd.commands;

import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import catalog.ICatalog;

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

    private final String tableName;

    public Display(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {

        this.catalog = catalog;
        this.sm = storageManager;

        // Display Info Syntax Validation
        List<String> input = getInput(args);
        if (!args.toLowerCase().contains("info") || input.size() != 3) {
            throw new InvalidUsage(args, "Correct Usage: (display info <table>;)");
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
        // TODO
    }
}
