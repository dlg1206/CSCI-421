package cli.cmd;

import catalog.ICatalog;
import cli.cmd.commands.*;
import cli.cmd.exception.CommandException;
import cli.cmd.exception.UnknownCommand;
import sm.StorageManager;

/**
 * <b>File:</b> CommandFactory.java
 * <p>
 * <b>Description:</b> Create Command Objects using input from user
 *
 * @author Derek Garcia
 */
public class CommandFactory {
    public static Command buildCommand(String args, ICatalog catalog, StorageManager storageManager) throws CommandException {
        // Parse the command keyword
        String cmdKeyword = args.split(" ")[0].replace(";", "");

        // Create new Command object based off keyword, error if unrecognized
        // Command constructors handle any bad args / input
        return switch (cmdKeyword.toLowerCase()) {
            case "create" -> new CreateTable(args, catalog, storageManager);
            case "drop" -> new DropTable(args, catalog, storageManager);
            case "alter" -> new AlterTable(args, catalog, storageManager);
            case "insert" -> new InsertInto(args, catalog, storageManager);
            case "display" -> new Display(args, catalog, storageManager);
            case "select" -> new Select(args, catalog, storageManager);
            default -> throw new UnknownCommand(cmdKeyword);
        };
    }
}
