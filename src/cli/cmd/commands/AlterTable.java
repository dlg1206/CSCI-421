package cli.cmd.commands;

import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;

/**
 * <b>File:</b> AlterTable.java
 * <p>
 * <b>Description: Command to alter a table in the database</b>
 *
 * @author Derek Garcia
 */
public class AlterTable extends Command {
    public AlterTable(String args) throws InvalidUsage {
        // Alter Table Syntax Validation
        String[] input = args.strip().split(" ");
        String errorMessage = "Correct Usage: (alter table <name> drop <a_name>;" +
                            "\n alter table <name> add <a_name> <a_type>;" +
                            "\n alter table <name> add <a_name> <a_type> default <value>);";

        if (input.length < 5 || !input[1].equalsIgnoreCase("table")) {
            throw new InvalidUsage(args, errorMessage);
        }

        boolean isValid = switch (input.length) {
            case 5 -> input[3].equalsIgnoreCase("drop");
            case 6 -> input[3].equalsIgnoreCase("add");
            default -> input[3].equalsIgnoreCase("add") && input[6].equalsIgnoreCase("default");
        };

        if (!isValid) {
            throw new InvalidUsage(args, errorMessage);
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
