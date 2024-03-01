package util;

/**
 * <b>File:</b> Tester.java
 * <p>
 * <b>Description:</b> Compares strings to determine if things were executed
 *
 * @author Derek Garcia
 */
public class Tester {
    private static final String RESET = "\033[0m";
    private static final String RED = "\033[0;31m";
    private static final String GREEN = "\033[0;32m";
    private final String testName;


    /**
     * Create new tester
     *
     * @param testName Name of the test
     */
    public Tester(String testName){
        this.testName = testName;
    }

    /**
     * Build a diff message
     *
     * @param command Command that was executed
     * @param expected Expected String
     * @param actual Actual String
     * @return Formatted diff message
     */
    private String buildMessage(String command, String expected, String actual){
        return new StrBuilder()
                .addLine("COMMAND: " + command)
                .addLine("===Expected===")
                .skipLine()
                .addLine(expected.trim())
                .skipLine()
                .addLine("====Actual====")
                .skipLine()
                .addLine(actual.trim())
                .skipLine()
                .addLine("==============")
                .build();
    }

    /**
     * Test if the strings match
     *
     * @param command Command that was executed
     * @param expected Expected String
     * @param actual Actual String
     * @return 0 if match, 1 otherwise
     */
    public int isEquals(String command, String expected, String actual){
        boolean isEquals = expected.trim().equals(actual.trim());
        String msg = new StrBuilder()
                .addLine((isEquals ? GREEN : RED) + "TEST: " + this.testName)
                .addLine(isEquals ? "STATUS: PASSED!" : "STATUS: FAILED!")
                .addLine(buildMessage(command, expected, actual) + RESET)
                .build();

        System.out.print(msg);

        return isEquals ? 0 : 1;
    }
}
