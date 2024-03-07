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

/**
 * <b>File:</b> Update.java
 * <p>
 * <b>Description: Command to Update information in the database</b>
 *
 */
public class Update extends Command{

    private static final Pattern FULL_MATCH = Pattern.compile(
        "update\\s+([a-z0-9]+)\\s+set\\s+([a-z0-9_]+)\\s*=\\s*" +
        "([a-z0-9_]+|'[^']*'|\"[^\"]*\"|true|false|\\d+(?:\\.\\d+)?)\\s*(where\\s+.+)?;", 
        Pattern.CASE_INSENSITIVE);

    private static final Pattern EACH_CONDITIONAL_MATCH = Pattern.compile(
        "([a-z0-9_]+)\\s*(=|>|<|>=|<=|!=)\\s*" +
        "([a-z0-9_]+|'[^']*'|\"[^\"]*\"|true|false|\\d+(?:\\.\\d+)?)\\s*",
        Pattern.CASE_INSENSITIVE);

    private final ICatalog catalog;
    private final StorageManager sm;
    private final String tableName;
    private final Set<String> attributeNames;
    private final HashMap<String, List<String>> conditionMap;

    public Update(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {
        this.catalog = catalog;
        this.sm = storageManager;
        this.conditionMap = new HashMap<>();
    
        Matcher fullMatcher = FULL_MATCH.matcher(args);
    
        if (!fullMatcher.matches()) {
            throw new InvalidUsage(args, "Correct Usage: update <name> set <column_1> = <value> where <condition>;");
        }

        
    
        String tableName = fullMatcher.group(1).toLowerCase();
        this.tableName = tableName;
        validateTableName(tableName);
        validateTableExists(tableName);
        
        String columnName = fullMatcher.group(2);
        validateAttributeExists(columnName, tableName, args);
    
    
        String conditions = fullMatcher.group(4) != null ? fullMatcher.group(4) : "";
        Set<String> attributeNames = new HashSet<>();
        if (!conditions.isEmpty()) {
            attributeNames = extractAttributeNames(conditions);
            for (String attr : attributeNames) {
                validateAttributeExists(attr, tableName, args);
            }
            validateConditions(conditions); 
            parseConditionsIntoMap(conditions);
        }
        this.attributeNames = attributeNames;
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
            ? args.split("where")[1].trim().replace(";", "")
            : "";

        if (!conditionals.isEmpty()) {
            String[] conditions = conditionals.split("(?i)\\s+(and|or)\\s+");
            for (String condition : conditions) {
                if (!EACH_CONDITIONAL_MATCH.matcher(condition).matches()) {
                    throw new InvalidUsage(args, "Using an Invalid Conditional");
                }
            }
        }
    }


    @Override
    protected void helpMessage() {
        // TODO
    }

    @Override
    public void execute() throws ExecutionFailure {
        // Logic to perform update operation
    }

}
