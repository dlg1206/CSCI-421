package cli.cmd.commands;
import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import catalog.Attribute;
import catalog.ICatalog;
import catalog.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import dataTypes.AttributeType;
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
        validateConditions(args);


        if (fullMatcher.group(2) != null) {
            try{
                this.whereTree = new WhereTree(fullMatcher.group(2), this.catalog, List.of(this.tableName));
            } catch (ExecutionFailure e){
                throw new InvalidUsage(args, e.getMessage());
            }
        }

    }

    private Set<String> extractAttributeNames(String conditions) {
        Set<String> attributeNames = new HashSet<>();
        Matcher conditionMatcher = EACH_CONDITIONAL_MATCH.matcher(conditions);

        while (conditionMatcher.find()) {
            attributeNames.add(conditionMatcher.group(1));
        }

        return attributeNames;
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

    private void validateAttributeExists(String attr, String tableName, String args) throws InvalidUsage {
        Table table = catalog.getRecordSchema(tableName);
        List<Attribute> attributes = table.getAttributes();
        Boolean temp = false;
        for (Attribute a : attributes) {
            if (a.getName().equals(attr)) {
                temp = true;
            }
        }
        if(!temp){
            throw new InvalidUsage(args, "Table " + tableName + " does not contain the attribute " + attr + "\nERROR");
        }
        
    }

    private void validateConditions(String args) throws InvalidUsage {
        String conditionals = args.toLowerCase().contains("where")
            ? args.substring(args.toLowerCase().indexOf("where") + 5).trim().replace(";", "")
            : "";
    
        if (!conditionals.isEmpty()) {
            if (conditionals.trim().matches(".*(and|or)\\s*$")) {
                throw new InvalidUsage(args, "Invalid conditional expression: ");
            }
    
            String[] conditions = conditionals.split("\\s+(and|or)\\s+");
            for (String condition : conditions) {
                parseConditionsIntoMap(condition);
                if (!EACH_CONDITIONAL_MATCH.matcher(condition.trim()).matches()) {
                    throw new InvalidUsage(args, "Invalid conditional expression: " + condition);
                }
            }
        }
    }
    

    private void parseConditionsIntoMap(String conditions) {
        Matcher conditionMatcher = EACH_CONDITIONAL_MATCH.matcher(conditions);
        while (conditionMatcher.find()) {
            String attribute = conditionMatcher.group(1);
            String condition = conditionMatcher.group(0); // The entire condition matched

            conditionMap.putIfAbsent(attribute, new ArrayList<>());
            conditionMap.get(attribute).add(condition);
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
