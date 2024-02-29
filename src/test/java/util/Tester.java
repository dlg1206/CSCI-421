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
        return new StrBuilder()
                .addLine("COMMAND: " + command)
                .addLine("===Expected===")
                .addLine(expected)
                .skipLine()
                .addLine("====Actual====")
                .addLine(actual)
                .skipLine()
                .addLine("==============")
                .build().strip();
    }

    public int isEquals(String command, String expected, String actual){
        boolean isEquals = expected.equals(actual);
        String msg = new StrBuilder()
                .addLine(isEquals ? GREEN : RED)
                .addLine("TEST: " + this.testName)
                .addLine(isEquals ? "STATUS: PASSED!" : "STATUS: FAILED!")
                .addLine(buildMessage(command, expected, actual))
                .addLine(RESET)
                .build();

        System.out.println(msg);

        return isEquals ? 0 : 1;
    }
}
