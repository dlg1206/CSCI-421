package cli;

import cli.cmd.CommandFactory;
import cli.cmd.commands.Command;
import cli.cmd.execption.CommandException;
import cli.util.Console;

/**
 * <b>File:</b> CLI.java
 * <p>
 * <b>Description: Main interface for CLI operations</b>
 *
 * @author Derek Garcia
 */
public class CLI {

    /**
     * Code to be executed prior to launching the cli
     */
    private void before(){
        // TODO - List commands? may remove or move to main for post operations separate from cli
        System.out.println("Hello!");
        System.out.println("exit; - quit the CLI");
    }

    /**
     * Code to be executed after to launching the cli
     */
    private void after(){
        // TODO - may remove or move to main for post operations separate from cli
        System.out.println("Goodbye!");
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
     * Start the CLI
     */
    public void run(){
        before();   // exe any tasks before running

        // Read until reach 'exit;` keyword
        while(true){
            String stdin = readInput();

            if(stdin.equalsIgnoreCase("exit;"))
                break;

            // Try to build and execute the command
            try{
                Command cmd = CommandFactory.buildCommand(stdin);
                cmd.execute();
            } catch (CommandException e){
                // fail if error with command
                Console.err(e.getMessage());
            }

        }

        after(); // exe any tasks after running
    }
}
