package util;

import java.util.List;

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
        StrBuilder msg = new StrBuilder()
                .addLine((isEquals ? GREEN : RED) + "TEST: " + this.testName)
                .addLine((isEquals ? "STATUS: PASSED!" : "STATUS: FAILED!" ) + RESET);

        // Only show diff if err
        if(!isEquals){
            msg.addLine(buildMessage(command, expected, actual));
        } else {
            msg.skipLine();
        }


        System.out.print(msg.build());

        return isEquals ? 0 : 1;
    }

    /**
     * Test if the strings match
     *
     * @param command Command that was executed
     * @param expected Expected String
     * @param actual Actual String
     * @return 0 if match, 1 otherwise
     */
    public int isUnorderedEquals(String command, String expected, String actual){

        List<String> expectedRows = List.of(expected.split("\n"));
        String expectedHeader = String.join("\n", expectedRows.subList(0, 3));
        List<String> finalExpectedRows = expectedRows.subList(3, expectedRows.size());

        List<String> actualRows = List.of(expected.split("\n"));
        String actualHeader = String.join("\n", actualRows.subList(0, 3));
        List<String> finalActualRows = actualRows.subList(3, actualRows.size());

        List<String> missingRows = finalExpectedRows.stream()
                .filter(i -> !finalActualRows.contains(i)).toList();

        List<String> extraRows = finalActualRows.stream()
                .filter(i -> !finalExpectedRows.contains(i)).toList();

        boolean isEquals = missingRows.isEmpty() && extraRows.isEmpty() && expectedHeader.equals(actualHeader);
        StrBuilder msg = new StrBuilder()
                .addLine((isEquals ? GREEN : RED) + "TEST: " + this.testName)
                .addLine((isEquals ? "STATUS: PASSED!" : "STATUS: FAILED!" ) + RESET);

        // Only show diff if err
        if(!isEquals){
            msg.addLine(buildMessage(command, expected, actual));
        } else {
            msg.skipLine();
        }


        System.out.print(msg.build());

        return isEquals ? 0 : 1;
    }
}
