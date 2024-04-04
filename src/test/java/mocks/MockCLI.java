package mocks;

import catalog.Catalog;
import cli.cmd.CommandFactory;
import cli.cmd.commands.Command;
import cli.cmd.exception.CommandException;
import sm.StorageManager;
import util.Console;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;

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
     * Will default to not use the index
     *
     * @param dbRoot Root path of database
     * @param pageSize Page size in bytes
     * @param bufferSize Number of pages buffer can hold
     */
    public MockCLI(String dbRoot, int pageSize, int bufferSize){
        PrintStream stdout = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));    // temp suppress output
        this.DBCatalog = new Catalog(pageSize, bufferSize, dbRoot, false);
        this.DBStorageManager = this.DBCatalog.StorageManager;
        System.setOut(stdout);
    }

    /**
     * Mimicked Constructor of the actual CLI class
     *
     * @param dbRoot Root path of database
     * @param pageSize Page size in bytes
     * @param bufferSize Number of pages buffer can hold
     * @param index Boolean to determine whether to use an index or not
     */
    public MockCLI(String dbRoot, int pageSize, int bufferSize, boolean index){
        PrintStream stdout = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));    // temp suppress output
        this.DBCatalog = new Catalog(pageSize, bufferSize, dbRoot, index);
        this.DBStorageManager = this.DBCatalog.StorageManager;
        System.setOut(stdout);
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
