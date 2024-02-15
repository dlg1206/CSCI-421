package cli.cmd.commands;

import catalog.ICatalog;
import cli.cmd.exception.ExecutionFailure;
import sm.StorageManager;

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
    public abstract void execute(ICatalog catalog, StorageManager sm) throws ExecutionFailure;
}
