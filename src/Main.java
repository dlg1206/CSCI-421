import catalog.Catalog;
import cli.CLI;
import sm.StorageManager;

/**
* <b>File:</b> Main.java
* <p>
* <b>Description:</b> Entrypoint to the project
* @author Derek Garcia
*/
public class Main {

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

    public static void main(String[] args) {

        try{
            validateArgs(args);
        } catch (Exception e) {
            System.err.println("Failed to validate arguments!");
            System.err.println("Reason: " + e.getMessage());
            System.err.println("Expected Usage: java Main <db loc> <page size> <buffer size>");
            System.err.println("\t<db loc>:         Path to the database root");
            System.err.println("\t<page size>:      Size of page ( in bytes )");
            System.err.println("\t<buffer size>:    Size of page buffer ( page capacity )");
        }

        int bufferSize = Integer.parseInt(args[2]);
        int pageSize = Integer.parseInt(args[1]);

        CLI cli = new CLI(
                new Catalog(pageSize, bufferSize, args[0]),
                new StorageManager(bufferSize, pageSize, args[0])   // args flipped for 10% increase in efficiency
        );

        cli.run();
    }

}
