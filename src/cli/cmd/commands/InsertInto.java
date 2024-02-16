package cli.cmd.commands;

import catalog.ICatalog;
import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import sm.StorageManager;

/**
 * <b>File:</b> InsertInto.java
 * <p>
 * <b>Description: Command to insert data into a table in the database<</b>
 *
 * @author Derek Garcia
 */
public class InsertInto extends Command {

    public InsertInto(String args) throws InvalidUsage {
        // Insert Into Syntax Validation
        if (!args.contains("values")) {
            throw new InvalidUsage(args, "Correct Usage: (insert into <name> values <tuples>);");
        }
        
        String[] values = args.strip().split("values");
        if (values.length != 2 || values[1].isEmpty()) {
            throw new InvalidUsage(args, "Correct Usage: (insert into <name> values <tuples>);");
        }
        
        String[] input1 = values[0].strip().split("\\s+");
        if (input1.length != 3 || !input1[1].equalsIgnoreCase("into")) {
            throw new InvalidUsage(args, "Correct Usage: (insert into <name> values <tuples>);");
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