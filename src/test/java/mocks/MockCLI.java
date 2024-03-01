package mocks;

import catalog.Catalog;
import cli.cmd.CommandFactory;
import cli.cmd.commands.Command;
import cli.cmd.exception.CommandException;
import sm.StorageManager;
import util.Console;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * <b>File:</b> MockCLI.java
 * <p>
 * <b>Description:</b> MockCLI to pass in commands to for testing
 *
 * @author Derek Garcia
 */
public class MockCLI {

    private final PrintStream stdout = System.out;
    private final Catalog DBCatalog;
    private final StorageManager DBStorageManager;


    /**
     * Mimicked Constructor of the actual CLI class
     *
     * @param catalog Catalog with table metadata
     * @param storageManager StorageManager to access hardware
     */
    public MockCLI(Catalog catalog, StorageManager storageManager) {
        DBCatalog = catalog;
        DBStorageManager = storageManager;
    }

    /**
     * Mock input to the CLI
     *
     * @param stdin Input to the CLI
     * @return Stdout result of the given command
     */
    public String mockInput(String stdin) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));   // record output

        try {
            Command cmd = CommandFactory.buildCommand(stdin, this.DBCatalog, this.DBStorageManager);
            cmd.execute();
        } catch (CommandException e) {
            // fail if error with command
            Console.err(e.getMessage());
        }

        System.setOut(new PrintStream(this.stdout));    // reset output

        return baos.toString()
                .replace("\r", "")      // Remove carriage return ( windows )
                .replaceAll("\u001B\\[[;\\d]*m", "");   // remove any color codes

    }
}
