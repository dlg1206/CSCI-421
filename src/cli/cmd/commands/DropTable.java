package cli.cmd.commands;

import catalog.ICatalog;
import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import sm.StorageManager;

/**
 * <b>File:</b> DropTable.java
 * <p>
 * <b>Description: Command to create a new table in the database</b>
 *
 * @author Derek Garcia
 */
public class DropTable extends Command {

    public DropTable(String args) throws InvalidUsage {
        // Drop Table Syntax Validation
        String[] input = args.strip().split(" ");
        if (!args.toLowerCase().contains("table") || input.length != 3) {
            throw new InvalidUsage(args, "Correct Usage: (drop table <table>;)");
        }
    }

    @Override
    protected void helpMessage() {
        // TODO
    }

    @Override
    public void execute(ICatalog catalog, StorageManager sm) throws ExecutionFailure {
        // TODO
    }
}
