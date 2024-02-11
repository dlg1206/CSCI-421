package cli.catalog;

/**
 * <b>File:</b> NotSupportedConstraint.java
 * <p>
 * <b>Description: An exception that is thrown when there is an attempt to get the max length for an attribute that
 * does not have that value. E.g. Int, Boolean, etc.</b>
 *
 * @author Clinten Hopkins
 */
public class NotSupportedConstraint extends Exception{
    /**
     * @param msg Execution Message
     */
    public NotSupportedConstraint(String msg) {
        super("Not Supported Constraint: " + msg);
    }
}