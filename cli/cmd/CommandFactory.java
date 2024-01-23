package cli.cmd;

import cli.cmd.commands.Command;
import cli.cmd.execption.CommandException;
import cli.cmd.execption.UnknownCommand;

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
        switch(cmdKeyword.toLowerCase()){
            default:
                throw new UnknownCommand(cmdKeyword);
        }
    }
}
