package cli;

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
        // TODO - List commands?
        System.out.println("Hello!");
        System.out.println("exit; - quit the CLI");
    }

    /**
     * Code to be executed after to launching the cli
     */
    private void after(){
        // TODO - may remove
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

        }

        after(); // exe any tasks after running
    }
}
