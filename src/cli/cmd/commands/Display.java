package cli.cmd.commands;

import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import catalog.ICatalog;
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

    private ICatalog catalog;

    public Display(String args) throws InvalidUsage {
        // Display Info Syntax Validation
        String[] input = args.strip().split(" ");
        if (!args.toLowerCase().contains("info") || input.length != 3) {
            throw new InvalidUsage(args, "Correct Usage: (display info <table>;)");
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
