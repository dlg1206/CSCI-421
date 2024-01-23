package cli.cmd.commands;

import cli.cmd.execption.ExecutionFailure;

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
}
