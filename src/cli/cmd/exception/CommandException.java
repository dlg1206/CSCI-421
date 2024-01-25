package cli.cmd.exception;

/**
 * <b>File:</b> CommandException.java
 * <p>
 * <b>Description: Generic Command Exception</b>
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
