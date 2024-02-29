package util;

/**
 * <b>File:</b> TestCaseStatus.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class Tester {
    private static final String RESET = "\033[0m";
    private static final String GREEN = "\033[0;32m";

    private static String buildMessage(String command, String expected, String actual){
        return  "Command: " + command + "\n" +
                "===Expected===\n" + expected + "\n" +
                "====Actual====\n" + actual + "\n==============\n";
    }

    public static int isEquals(String command, String expected, String actual){
        if( expected.equals(actual) ){
            System.out.println(GREEN + "Test Passed!\n" + buildMessage(command, expected, actual) + RESET);
            return 0;
        } else {
            System.err.println("Test Failed!\n" + buildMessage(command, expected, actual));
            return 1;
        }
    }
}
