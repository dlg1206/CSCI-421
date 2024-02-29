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
    private static final String RED = "\033[0;31m";
    private static final String GREEN = "\033[0;32m";
    private final String testName;


    public Tester(String testName){
        this.testName = testName;
    }
    private String buildMessage(String command, String expected, String actual){
        return  "COMMAND: " + command + "\n" +
                "===Expected===\n" + expected +
                "\n====Actual====\n" + actual + "\n==============";
    }

    public int isEquals(String command, String expected, String actual){
        if( expected.equals(actual) ){
            System.out.println(GREEN + "TEST: " + this.testName + "\nSTATUS: PASSED!\n" + buildMessage(command, expected, actual) + "\n" + RESET);
            return 0;
        } else {
            System.out.println(RED + "TEST: " + this.testName + "\nSTATUS: FAILED!\n" + buildMessage(command, expected, actual) + "\n" + RESET);
            return 1;
        }
    }
}
