package cli.cmd.commands;
import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import catalog.Attribute;
import catalog.ICatalog;
import catalog.Table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import dataTypes.AttributeType;
import dataTypes.DataType;
import util.Console;
import sm.StorageManager;
import util.where.WhereTree;

/**
 * <b>File:</b> Display.java
 * <p>
 * <b>Description: Command to Delete information about the database</b>
 *
 */
public class Delete extends Command{

    private static final Pattern FULL_MATCH = Pattern.compile(
        "delete\\s+from\\s+([a-z0-9]+)\\s*(where\\s+.+)?;", Pattern.CASE_INSENSITIVE);

    private static final Pattern EACH_CONDITIONAL_MATCH = Pattern.compile(
        "([a-z0-9_]+)\\s*(=|>|<|>=|<=|!=)\\s*" +
        "([a-z0-9_]+|'[^']*'|\"[^\"]*\"|true|false|\\d+(?:\\.\\d+)?)\\s*",
        Pattern.CASE_INSENSITIVE);

    private final ICatalog catalog;
    private final StorageManager sm;
    private final String tableName;
    private final HashMap<String, List<String>> conditionMap;
    private WhereTree whereTree;

    public Delete(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {
        this.catalog = catalog;
        this.sm = storageManager;
        this.conditionMap = new HashMap<>();

        Matcher fullMatcher = FULL_MATCH.matcher(args);

        if (!fullMatcher.matches()) {
            throw new InvalidUsage(args, "Correct Usage: delete from <name> where <condition>;");
        }

        String tableName = fullMatcher.group(1).toLowerCase();
        this.tableName = tableName;
        validateTableName(tableName);
        validateTableExists(tableName);

        if (fullMatcher.group(2) != null) {
            try{
                this.whereTree = new WhereTree(fullMatcher.group(2), this.catalog, List.of(this.tableName));
            } catch (ExecutionFailure e){
                throw new InvalidUsage(args, e.getMessage());
            }
        }

    }

    private void validateTableName(String tableName) throws InvalidUsage {
        if (!tableName.matches("[a-z][a-z0-9]*")) {
            throw new InvalidUsage(tableName, "The name '%s' is not a valid table name.".formatted(tableName));
        }
    }

    private void validateTableExists(String tableName) throws InvalidUsage {
        Set<String> allTables = catalog.getExistingTableNames();
        if (!allTables.contains(tableName)) {
            throw new InvalidUsage(tableName, "Table " + tableName + " does not exist in the catalog \nERROR");
        }
    }


    @Override
    protected void helpMessage() {
        // TODO
    }

    @Override
    public void execute() throws ExecutionFailure {
        int tableID = this.catalog.getTableNumber(this.tableName);
        int pki = this.catalog.getRecordSchema(this.tableName).getIndexOfPrimaryKey();
        List<Attribute> attributes = this.catalog.getRecordSchema(this.tableName).getAttributes();

        // For each record, if where clause matches delete from table
        for (List<DataType> record: this.sm.getAllRecords(tableID, attributes)) {
            try {
                // note: if no where clause is given, delete all records
                if (whereTree == null || this.whereTree.passesTree(record)) {
                    this.sm.deleteRecord(tableID, record.get(pki), attributes);
                }
            } catch (IOException e) {
                throw new ExecutionFailure("The file for the table '%s' could not be opened or modified.".formatted(tableName));
            }
        }
    }

    
}
