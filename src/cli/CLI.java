package cli;

import catalog.Catalog;
import cli.cmd.CommandFactory;
import cli.cmd.commands.Command;
import cli.cmd.exception.CommandException;
import util.Console;
import sm.StorageManager;

import java.io.*;

/**
 * <b>File:</b> CLI.java
 * <p>
 * <b>Description:</b> Main interface for CLI operations
 *
 * @author Derek Garcia
 */
public class CLI {

    /**
     * Output stream wrapper to suppressed CLI output
     */
    private static class CLIStandardOutput extends OutputStream {
        @Override
        public void write(int b){
            return;
        }
        @Override
        public void write(byte[] b){
            return;
        }
        @Override
        public void write(byte[] b, int off, int len){
            return;
        }

        private final PrintStream stdout;

        /**
         * Create new CLI Output Stream
         *
         * @param stdout output stream to print to
         */
        public CLIStandardOutput(PrintStream stdout){
            this.stdout = stdout;
        }

        /**
         * Set the original stdout out stream as the output
         */
        public void enable(){
            System.setOut(this.stdout);
        }

        /**
         * Set this as the output stream, redirecting any output set for the system output
         */
        public void disable(){
            System.setOut(new PrintStream(this));
        }
    }

    private final Catalog DBCatalog;
    private final StorageManager DBStorageManager;
    private final CLIStandardOutput outputStream;

    public CLI(Catalog catalog, StorageManager storageManager) {
        DBCatalog = catalog;
        DBStorageManager = storageManager;
        this.outputStream = new CLIStandardOutput(System.out);  // enabled by default
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
     * @param silent       Print mock input or not
     */
    public void runWith(String commandsPath, boolean silent){
        Console.debugMsg("Launching with CLI with the commands at " + commandsPath);
        boolean startCLI = true;    // start the cli after run
        // temp disable output
        if(silent) {
            Console.debugMsg("Running in silent mode, output will be suppressed");
            this.outputStream.disable();
        }

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

        } catch (Exception e) {
            Console.debugErr(e.toString());
        } finally {
            this.outputStream.enable();
        }

        if(startCLI){
            Console.debugMsg("Starting CLI. . .");
            run();
        } else {
            Console.debugMsg("Skipping CLI. . .");
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
