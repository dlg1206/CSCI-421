package cli.cmd.commands;

import catalog.ICatalog;
import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import sm.StorageManager;

/**
 * <b>File:</b> Display.java
 * <p>
 * <b>Description: Command to display information about the database</b>
 *
 * @author Derek Garcia
 */
public class Display extends Command {

    public Display(String args) throws InvalidUsage {
        // Display Info Syntax Validation
        String[] input = args.strip().split(" ");
        if (!args.toLowerCase().contains("info") || input.length != 3) {
            throw new InvalidUsage(args, "Correct Usage: (display info <table>;)");
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
