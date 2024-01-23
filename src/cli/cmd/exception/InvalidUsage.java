package cli.cmd.exception;

/**
 * <b>File:</b> InvalidUsage.java
 * <p>
 * <b>Description: Execution when command usage is wrong</b>
 *
 * @author Derek Garcia
 */
public class InvalidUsage extends CommandException{
    /**
     * Create a new Invalid Usage exception
     *
     * @param command Command that failed
     * @param msg Reason for failure
     */
    public InvalidUsage(String command, String msg){
        super("Invalid Usage (" + command +" ): " + msg);
    }
}
