package cli.cmd;

import cli.cmd.commands.*;
import cli.cmd.exception.CommandException;
import cli.cmd.exception.UnknownCommand;

/**
 * <b>File:</b> CommandFactory.java
 * <p>
 * <b>Description: Create Command Objects using input from user</b>
 *
 * @author Derek Garcia
 */
public class CommandFactory {
    public static Command buildCommand(String args) throws CommandException {
        // Parse the command keyword
        String cmdKeyword = args.split(" ")[0].replace(";", "");

        // Create new Command object based off keyword, error if unrecognized
        // Command constructors handle any bad args / input
        return switch (cmdKeyword.toLowerCase()) {
            case "create" -> new CreateTable(args);
            case "drop" -> new DropTable(args);
            case "alter" -> new AlterTable(args);
            case "insert" -> new InsertInto(args);
            case "display" -> new Display(args);
            case "select" -> new Select(args);
            default -> throw new UnknownCommand(cmdKeyword);
        };
    }
}
