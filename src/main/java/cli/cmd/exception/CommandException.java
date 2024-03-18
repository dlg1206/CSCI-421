package cli.cmd.exception;

/**
 * <b>File:</b> CommandException.java
 * <p>
 * <b>Description:</b> Generic Command Exception
 *
 * @author Derek Garcia
 */
public abstract class CommandException extends Exception{
    /**
     * @param msg Execution Message
     */
    public CommandException(String msg) {
        super(msg);
    }
}
