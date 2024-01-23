package cli.util;

import java.util.Scanner;

/**
 * <b>File:</b> Console.java
 * <p>
 * <b>Description: Wrapper for command-line printing</b>
 *
 * @author Derek Garcia
 */
public class Console {
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
     * Get input from stdin
     *
     * @return input from stdin
     */
    public static String in(){
        System.out.print(PREFIX);
        return SCANNER.nextLine();
    }
}
