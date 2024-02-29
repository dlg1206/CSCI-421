import catalog.Catalog;
import mocks.MockCLI;
import mocks.MockStdoutBuilder;
import util.Tester;

/**
 * <b>File:</b> PhaseOneTest.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class TestRunner {
    private static MockCLI MOCK_CLI;

    private static String DB_ROOT;
    private static int PAGE_SIZE;
    private static int BUFFER_SIZE;

    public static int test_display_schema(){
        String expected = new MockStdoutBuilder()
                .addLine("DB location: " + DB_ROOT)
                .addLine("Page Size: " + PAGE_SIZE)
                .addLine("Buffer Size: " + BUFFER_SIZE)
                .skipLine()
                .addLine("No tables to display")
                .addLine("SUCCESS")
                .build();

        // Given
        String command = "display schema;";
        // When
        String actual = MOCK_CLI.mockInput(command);
        // Then
        return Tester.isEquals(command, expected, actual);
    }

    public static int test_display_info_for_missing_table(){
        String expected = new MockStdoutBuilder()
                .addLine("Invalid Usage (display info foo;): Table foo does not Exist in the Catalog ")
                .addLine("ERROR")
                .build();

        // Given
        String command = "display info foo;";
        // When
        String actual = MOCK_CLI.mockInput(command);
        // Then
        return Tester.isEquals(command, expected, actual);
    }

    public static void main(String[] args) {

        // Build CLI
        DB_ROOT = args[0];
        PAGE_SIZE = Integer.parseInt(args[1]);
        BUFFER_SIZE = Integer.parseInt(args[2]);

        // Make the catalog (initialize the DB)
        Catalog catalog = new Catalog(PAGE_SIZE, BUFFER_SIZE, DB_ROOT);

        MOCK_CLI = new MockCLI(
                catalog,
                catalog.StorageManager
        );

        System.out.println("Running Test Cases");
        System.out.println("\tBuffer Size: " + BUFFER_SIZE);
        System.out.println("\tPage Size: " + DB_ROOT);

        int exitCode = 0;
        int totalTest = 2;

        exitCode += test_display_schema();
        exitCode += test_display_info_for_missing_table();

        System.out.println("Tests Passed: " + (totalTest - exitCode));
        System.out.println("Tests Failed: " + exitCode);

        System.exit(exitCode > 0 ? 1 : 0);


    }


}
