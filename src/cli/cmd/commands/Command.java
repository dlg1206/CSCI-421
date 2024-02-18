package cli.cmd.commands;

import catalog.ICatalog;
import cli.cmd.exception.ExecutionFailure;
import sm.StorageManager;

import java.util.Arrays;
import java.util.List;

/**
 * <b>File:</b> Command.java
 * <p>
 * <b>Description: Generic Command object for database commands</b>
 *
 * @author Derek Garcia
 */
public abstract class Command {

    /**
     * Display help message for command
     */
    protected abstract void helpMessage();

    /**
     * Execute the command
     */
    public abstract void execute() throws ExecutionFailure;

    List<String> getInput (String args) {
        List<String> input = Arrays.asList(args.substring(0, args.length() - 1).strip().split(" "));
        input.removeIf(String::isEmpty);
        return input;
    }
}
