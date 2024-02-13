package cli.util;

import java.util.Scanner;

/**
 * <b>File:</b> Console.java
 * <p>
 * <b>Description:</b> Wrapper for command-line printing
 *
 * @author Derek Garcia
 */
public class Console {
    // Text colors
    public static final String RESET = "\033[0m";
    public static final String RED = "\033[0;31m";

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final String PREFIX = "> ";

    /**
     * Output to stdout
     *
     * @param msg Message to output
     */
    public static void out(String msg){
        System.out.println(msg);
    }

    /**
     * Output an error message
     *
     * @param err Error message to output
     */
    public static void err(String err){
        System.out.println(RED + err + RESET);
    }

    /**
     * Get input from stdin
     *
     * @return input from stdin
     */
    public static String in(){
        System.out.print(PREFIX);
        return SCANNER.nextLine();
    }
}
