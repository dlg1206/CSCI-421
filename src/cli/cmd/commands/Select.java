package cli.cmd.commands;

import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import cli.cmd.exception.UnknownCommand;

/**
 * <b>File:</b> Select.java
 * <p>
 * <b>Description: Command to select items from the database</b>
 *
 * @author Derek Garcia
 */
public class Select extends Command {

    public Select(String args) throws InvalidUsage {
        // Select String Syntax Validation
        String[] userInput = args.strip().split(" ");
        if(userInput.length != 4 || !userInput[1].equals("*") || !userInput[2].equalsIgnoreCase("from")){
            throw new InvalidUsage(args, "Correct Usage: (Select * From <table name>;)");
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
