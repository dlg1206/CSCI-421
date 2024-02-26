package cli.cmd.exception;

/**
 * <b>File:</b> ExecutionFailure.java
 * <p>
 * <b>Description:</b> Error while command was running
 *
 * @author Derek Garcia
 */
public class ExecutionFailure extends CommandException{

    /**
     * Create a new Execution exception
     *
     * @param msg reason for failure
     */
    public ExecutionFailure(String msg){
        super("Execution Failure: " + msg);

    }
}
