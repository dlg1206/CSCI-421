import catalog.Catalog;
import cli.CLI;

import java.util.List;

/**
* <b>File:</b> Main.java
* <p>
* <b>Description:</b> Entrypoint to the project
* @author Derek Garcia
*/
public class Main {

    /**
     * Validate given arguments
     *
     * @param args Run arguments
     * @throws Exception Failed validation
     */
    private static void validateArgs(String[] args) throws Exception {
        if(args.length < 3)
            throw new Exception("Missing arguments, expected 3 but got " + args.length);

        try { Integer.parseInt(args[1]); }
        catch (NumberFormatException e) {
            throw new Exception("Page size is not a valid number");
        }

        try { Integer.parseInt(args[2]); }
        catch (NumberFormatException e) {
            throw new Exception("Buffer Size is not a valid number");
        }
    }


    /**
     * Create or load a database and launch the CLI terminal
     *
     * @param args runtime args
     */
    public static void main(String[] args) {
        // Validate args
        try{
            validateArgs(args);
        } catch (Exception e) {
            System.err.println("Failed to validate arguments!");
            System.err.println("Reason: " + e.getMessage());
            System.err.println("Expected Usage: java Main <db loc> <page size> <buffer size> <index>");
            System.err.println("\t<db loc>:         Path to the database root");
            System.err.println("\t<page size>:      Size of page ( in bytes )");
            System.err.println("\t<buffer size>:    Size of page buffer ( page capacity )");
            System.err.println("\t<index>:          optional param, if true initialize database using indexes");
            System.exit(1);
        }

        // Build CLI
        int bufferSize = Integer.parseInt(args[2]);
        int pageSize = Integer.parseInt(args[1]);
        // Make the catalog (initialize the DB)
        Catalog catalog = new Catalog(
                pageSize, bufferSize,
                args[0],
                List.of("true", "false").contains(args[3]) && Boolean.parseBoolean(args[3])    // if index param present, convert to bool
        );

        CLI cli = new CLI(
                catalog,
                catalog.StorageManager
        );

        // If dev argument is present (2nd to last arg), run the CLI with those commands first
        // -d <path to cmds>: Dev
        // -ds <path to cmd>: Dev Silent
        if(args[args.length - 2].equals("-d") || args[args.length - 2].equals("-ds")){
            cli.runWith(
                    args[args.length - 1],
                    args[args.length - 2].equals("-ds")
            );
        } else {
            // else just start the CLI
            cli.run();
        }

    }

}
