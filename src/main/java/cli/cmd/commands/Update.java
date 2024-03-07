package cli.cmd.commands;
import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import catalog.Attribute;
import catalog.ICatalog;
import catalog.Table;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import dataTypes.AttributeType;
import util.Console;
import sm.StorageManager;

/**
 * <b>File:</b> Display.java
 * <p>
 * <b>Description: Command to Update information about the database</b>
 *
 * @author Derek Garcia
 */
public class Update extends Command {

    private final ICatalog catalog;
    private final StorageManager sm;

    private static final Pattern FULL_MATCH = Pattern.compile(
    "update\\s+([a-z0-9_]+)\\s+" + 
    "set\\s+([a-z0_]+)\\s*=\\s*([^\\s]+)\\s+" + 
    "where\\s+(.+?);", 
    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("[a-z][a-z0-9]*", Pattern.CASE_INSENSITIVE);

    public Update(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {

        this.catalog = catalog;
        this.sm = storageManager;

        Matcher fullMatcher = FULL_MATCH.matcher(args);

        if (!fullMatcher.matches()) {
            throw new InvalidUsage(args, """
                    Correct Usage: update <name>
                    set <column_1> = <value>
                    where <condition>;""");
        }

    }

    @Override
    protected void helpMessage() {
        // TODO
    }

    @Override
    public void execute() throws ExecutionFailure { 
        
    }

    
}
