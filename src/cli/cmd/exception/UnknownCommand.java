package cli.cmd.exception;

/**
 * <b>File:</b> UnknownCommand.java
 * <p>
 * <b>Description: Given command is unknown</b>
 *
 * @author Derek Garcia
 */
public class UnknownCommand extends CommandException{

    /**
     * Create a new UnknownCommand exception
     *
     * @param unknownCommand name of the unknown command
     */
    public UnknownCommand(String unknownCommand) {
        super("Unknown Command: '" + unknownCommand + "' is not a recognized command!");
    }
}
