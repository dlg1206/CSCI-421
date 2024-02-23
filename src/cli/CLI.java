package cli;

import catalog.Catalog;
import cli.cmd.CommandFactory;
import cli.cmd.commands.Command;
import cli.cmd.exception.CommandException;
import cli.util.Console;
import sm.StorageManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * <b>File:</b> CLI.java
 * <p>
 * <b>Description:</b> Main interface for CLI operations
 *
 * @author Derek Garcia
 */
public class CLI {

    private final Catalog DBCatalog;
    private final StorageManager DBStorageManager;

    public CLI(Catalog catalog, StorageManager storageManager) {
        DBCatalog = catalog;
        DBStorageManager = storageManager;
    }

    /**
     * Code to be executed prior to launching the cli
     */
    private void before(){
        // TODO - List commands? may remove or move to main for post operations separate from cli
        Console.out("Hello!");
        Console.out("exit; - quit the CLI");
    }

    /**
     * Code to be executed after to launching the cli
     */
    private void after(){
        try {
            DBStorageManager.flush();
        } catch (IOException ioe) {
            Console.err("This db is corrupt...");
        }
        Console.out("Goodbye!");
    }

    /**
     * Read input until terminating semicolon
     *
     * @return String with no newlines terminated by a semicolon
     */
    private String readInput(){
        StringBuilder stdin = new StringBuilder();

        // Keep getting input until ';' character
        while (true) {
            String line = Console.in();
            stdin.append(line);

            if (line.endsWith(";"))
                break;

            stdin.append(" ");
        }
        return stdin.toString().trim();
    }

    /**
     * Attempt to execute user input
     *
     * @param stdin user input
     */
    private void executeStdin(String stdin){
        // Try to build and execute the command
        try{
            Command cmd = CommandFactory.buildCommand(stdin, this.DBCatalog, this.DBStorageManager);
            cmd.execute();
        } catch (CommandException e){
            // fail if error with command
            Console.err(e.getMessage());
        }
    }

    /**
     * Debug run command that runs a series of commands before launching the terminal
     *
     * @param commandsPath Path to list of commands
     */
    public void runWith(String commandsPath){
        Console.debugMsg("Launching with CLI with the commands at " + commandsPath);
        boolean startCLI = true;    // start the cli after run
        try (BufferedReader br = new BufferedReader(new FileReader(commandsPath))){
            String stdin = br.readLine();
            while (stdin != null) {
                // Skip '#' comment
                if(stdin.charAt(0) == '#'){
                    stdin = br.readLine();
                    continue;
                }

                // print the command as if user input it
                Console.mockInput(stdin);

                // exit if requested
                if(stdin.equalsIgnoreCase("exit;")){
                    after();
                    startCLI = false;
                    break;
                }
                executeStdin(stdin);
                stdin = br.readLine();
            }

        } catch (IOException e) {
            Console.debugErr(e.toString());
        } finally {
            if(startCLI){
                Console.debugMsg("Starting CLI. . .");
                run();
            } else {
                Console.debugMsg("Skipping CLI. . .");
            }
        }
    }

    /**
     * Start the CLI
     */
    public void run(){
        before();   // exe any tasks before running

        // Read until reach 'exit;` keyword
        while(true){
            String stdin = readInput();
            if(stdin.equalsIgnoreCase("exit;"))
                break;
            executeStdin(stdin);
        }

        after(); // exe any tasks after running
    }
}
