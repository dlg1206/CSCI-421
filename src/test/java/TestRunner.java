import catalog.Catalog;
import mocks.MockCLI;
import mocks.MockStdoutBuilder;
import util.Tester;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;

/**
 * <b>File:</b> PhaseOneTest.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class TestRunner {

    private static String DB_ROOT;
    private static int PAGE_SIZE;
    private static int BUFFER_SIZE;
    
    private static MockCLI buildMockCLI(){
        cleanUp();  // remove old db
        PrintStream stdout = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));    // temp suppress output
        // Make the catalog (initialize the DB)
        Catalog catalog = new Catalog(PAGE_SIZE, BUFFER_SIZE, DB_ROOT);
        System.setOut(stdout);
        return new MockCLI(
                catalog,
                catalog.StorageManager
        );
    }

    private static void cleanUp(){
        for(File file: Objects.requireNonNull(new File(DB_ROOT).listFiles()))
            if (!file.isDirectory())
                file.delete();
    }

    private static int test_display_schema(){
        String expected = new MockStdoutBuilder()
                .addLine("DB location: " + DB_ROOT)
                .addLine("Page Size: " + PAGE_SIZE)
                .addLine("Buffer Size: " + BUFFER_SIZE)
                .skipLine()
                .addLine("No tables to display")
                .addLine("SUCCESS")
                .build();

        // Given
        MockCLI mockCLI = buildMockCLI();
        String command = "display schema;";
        // When
        String actual = mockCLI.mockInput(command);
        // Then
        return Tester.isEquals(command, expected, actual);
    }

    private static int test_display_info_for_missing_table(){
        String expected = new MockStdoutBuilder()
                .addLine("Invalid Usage (display info foo;): Table foo does not Exist in the Catalog ")
                .addLine("ERROR")
                .build();

        // Given
        MockCLI mockCLI = buildMockCLI();
        String command = "display info foo;";
        // When
        String actual = mockCLI.mockInput(command);
        // Then
        return Tester.isEquals(command, expected, actual);
    }

    private static int test_select_from_missing_table(){
        String expected = "Invalid Usage (select * from foo;): Table foo does not exist in the Catalog\n";

        // Given
        MockCLI mockCLI = buildMockCLI();
        String command = "select * from foo;";
        // When
        String actual = mockCLI.mockInput(command);
        // Then
        return Tester.isEquals(command, expected, actual);
    }

    private static int test_create_valid_table(){
        // Given
        String command = "create table foo( id integer primarykey);";
        // When
        MockCLI mockCLI = buildMockCLI();
        String actual = mockCLI.mockInput(command);
        // Then
        // todo actually check table was made
        return Tester.isEquals(command, "", actual);
    }

    private static int test_display_table_info(){
        String expected = new MockStdoutBuilder()
                .addLine("Table Name: foo")
                .addLine("Table Schema: ")
                .addLine("     id:integer primarykey")
                .addLine("Pages: 0")
                .addLine("Records: 0")
                .addLine("SUCCESS")
                .build();

        // Given
        MockCLI mockCLI = buildMockCLI();
        String command = "display info foo;";
        // When
        mockCLI.mockInput("create table foo( id integer primarykey);");
        String actual = mockCLI.mockInput(command);
        // Then
        return Tester.isEquals(command, expected, actual);
    }

    private static int test_display_schema_with_one_table(){
        String expected = new MockStdoutBuilder()
                .addLine("DB location: " + DB_ROOT)
                .addLine("Page Size: " + PAGE_SIZE)
                .addLine("Buffer Size: " + BUFFER_SIZE)
                .addLine("Tables: ")
                .skipLine()
                .addLine("Table Name: foo")
                .addLine("Table Schema: ")
                .addLine("     id:integer primarykey")
                .addLine("Pages: 0")
                .addLine("Records: 0")
                .skipLine()
                .skipLine()
                .addLine("SUCCESS")
                .build();

        // Given
        MockCLI mockCLI = buildMockCLI();
        String command = "display schema;";
        // When
        mockCLI.mockInput("create table foo( id integer primarykey);");
        String actual = mockCLI.mockInput(command);
        // Then
        return Tester.isEquals(command, expected, actual);
    }

    private static int test_select_from_empty_table(){
        String expected = new MockStdoutBuilder()
                .addLine("-------")
                .addLine("| id  |")
                .addLine("-------")
                .skipLine()
                .build();

        // Given
        MockCLI mockCLI = buildMockCLI();
        String command = "select * from foo;";
        // When
        mockCLI.mockInput("create table foo( id integer primarykey);");
        String actual = mockCLI.mockInput(command);
        // Then
        return Tester.isEquals(command, expected, actual);
    }


    public static void main(String[] args) {

        // Build CLI
        DB_ROOT = args[0];
        PAGE_SIZE = Integer.parseInt(args[1]);
        BUFFER_SIZE = Integer.parseInt(args[2]);

        System.out.println("Running Test Cases");
        System.out.println("\tBuffer Size: " + BUFFER_SIZE);
        System.out.println("\tPage Size: " + DB_ROOT);

        int exitCode = 0;

        exitCode += test_display_schema();
        exitCode += test_display_info_for_missing_table();
        exitCode += test_select_from_missing_table();
        exitCode += test_create_valid_table();
        exitCode += test_display_table_info();
        exitCode += test_display_schema_with_one_table();
        exitCode += test_select_from_empty_table();


        System.out.println("Tests Failed: " + exitCode);

        System.exit(exitCode > 0 ? 1 : 0);


    }


}
