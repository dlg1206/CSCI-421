package cli.cmd.commands;
import cli.cmd.exception.*;
import catalog.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import dataTypes.*;
import util.where.WhereTree;
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
        "([a-z0-9_]+|'[^']*'|\"[^\"]*\"|true|false|-?\\d+(?:\\.\\d+)?)\\s*(where\\s+.+)?;",
        Pattern.CASE_INSENSITIVE);

    private static final String INVALID_ATTR_LENGTH_MSG = "The attribute '%s' has a max length of %s characters. You provided too many characters in tuple #%s";
    private static final String NO_QUOTES_MSG = "The attribute '%s' takes a string, which must be wrapped in quotes. You did not do this for tuple #%s";
    private static final Pattern STRING_PATTERN = Pattern.compile("\"(.*)\"", Pattern.CASE_INSENSITIVE);

    private static final String UNEQUAL_ATTR_MSG = "Table %s expects %s attributes and you provided %s.";
    private static final String INVALID_ATTR_TYPE_MSG = "The provided value '%s' for attribute '%s' is not of type %s.";
    private static final Pattern VALUE_PATTERN = Pattern.compile("\".*?\"|\\S+", Pattern.CASE_INSENSITIVE);
    private final ICatalog catalog;
    private final StorageManager sm;
    private final String tableName;
    private final String updateValue;
    private Attribute setAttribute;
    private WhereTree whereTree = null;
    private Attribute primaryKey;
    private int primaryKeyIdx;
    private Integer attributeIndex;
    private final String columnName;

    public Update(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {
        this.catalog = catalog;
        this.sm = storageManager;
    
        Matcher fullMatcher = FULL_MATCH.matcher(args);
    
        if (!fullMatcher.matches()) {
            throw new InvalidUsage(args, "Correct Usage: update <name> set <column_1> = <value> where <condition>;");
        }

        String tableName = fullMatcher.group(1).toLowerCase();
        this.tableName = tableName;
        this.updateValue = fullMatcher.group(3);
        validateTableName(tableName);
        validateTableExists(tableName);
        
        this.columnName = fullMatcher.group(2);
        validateAttributeExists(columnName, tableName, args);
        // Validating if column contains set value type
        try {
            switch(setAttribute.getDataType()){
                case INTEGER -> new DTInteger(updateValue);
                case DOUBLE -> new DTDouble(updateValue);
                case BOOLEAN -> new DTBoolean(updateValue);
                case CHAR -> new DTChar(updateValue, setAttribute.getMaxDataLength());
                case VARCHAR -> new DTVarchar(updateValue);
            }
        } catch (NumberFormatException nfe) {
            throw new InvalidUsage(args, "Cannot set %s to value %s".formatted(columnName, updateValue));
        }
        if ((setAttribute.getDataType() == AttributeType.CHAR || setAttribute.getDataType() == AttributeType.VARCHAR) && updateValue != null) {
            Matcher stringMatcher = STRING_PATTERN.matcher(updateValue);

            if (!stringMatcher.matches()) {
                throw new InvalidUsage(NO_QUOTES_MSG.formatted(setAttribute.getName(), columnName), "");
            }

            String val = stringMatcher.group(1);

            if (val.length() > setAttribute.getMaxDataLength()) {
                throw new InvalidUsage(INVALID_ATTR_LENGTH_MSG.formatted(setAttribute.getName(), setAttribute.getMaxDataLength(), columnName), "");
            }
        }
        else if(setAttribute.getDataType() == AttributeType.BOOLEAN && updateValue != null){
            if(!(updateValue.equalsIgnoreCase("true") || updateValue.equalsIgnoreCase("false"))){
                throw new InvalidUsage(updateValue, "Booleans only accept true/false values");
            }
        }
        
        String allConditions = fullMatcher.group(4);
        if (allConditions != null) {
            try {
                whereTree = new WhereTree(allConditions, catalog, List.of(tableName));
            } catch (ExecutionFailure ef) {
                throw new InvalidUsage(args, ef.getMessage());
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

    private void validateAttributeExists(String attr, String tableName, String args) throws InvalidUsage {
        Table table = catalog.getRecordSchema(tableName);
        List<Attribute> attributes = table.getAttributes();
        boolean temp = false;
        int count = 0;
        for (Attribute a : attributes) {
            if(a.isPrimaryKey()){
                primaryKey = a;
                primaryKeyIdx = count;
            }
            if (a.getName().equalsIgnoreCase(attr)) {
                temp = true;
                setAttribute = a;
            }
            if(a.getName().equalsIgnoreCase(columnName)){
                attributeIndex = count;
            }
            count++;
        }
        if(!temp){
            throw new InvalidUsage(args, "Table " + tableName + " does not contain the attribute " + attr + "\nERROR");
        }
        
    }

    @Override
    protected void helpMessage() {
        // TODO
    }

    @Override
    public void execute() throws ExecutionFailure {
        int tableID = this.catalog.getTableNumber(this.tableName);
        List<Attribute> attributes = this.catalog.getRecordSchema(this.tableName).getAttributes();
        int PKIndex = catalog.getRecordSchema(tableName).getIndexOfPrimaryKey();

        List<List<DataType>> allRecords = whereTree == null
                ? this.sm.getAllRecords(tableID, attributes)
                : this.sm.selectRecords(tableID, attributes, whereTree);

        for (List<DataType> record : allRecords) {
            String deleteCommand = "DELETE FROM " + tableName + " WHERE " + primaryKey.getName() + " = " + record.get(primaryKeyIdx).stringValue() + ";";
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < record.size(); i++) {
                if(i == attributeIndex){
                    values.append(updateValue).append(" ");
                }
                else{
                    values.append(record.get(i).stringValue()).append(" ");
                }
            }

            values = new StringBuilder(values.toString().replace("\"", "").strip());
            try {
                // Run each command
                List<DataType> tuple = convertStringToTuple(values.toString(), attributes);
                checkUniqueConstraint(tableID, attributes, tuple);
                
                Delete newDeleteExecutable = new Delete(deleteCommand, this.catalog, this.sm);
                newDeleteExecutable.execute();

                try {
                    if (sm.getAllRecords(tableID, attributes).stream().anyMatch(r -> r.get(PKIndex).compareTo(record.get(PKIndex)) == 0))
                        throw new ExecutionFailure("There already exists an entry for primary key: '%s'.".formatted(record.get(PKIndex).stringValue()));

                    sm.insertRecord(tableID, attributes, tuple);
                } catch (IOException ioe) {
                    throw new ExecutionFailure("The file for the table '%s' could not be opened or modified.".formatted(tableName));
                }
            } catch (InvalidUsage e) {
                throw new ExecutionFailure("Execution failure to update record where " + primaryKey.getName() + " = " + record.get(primaryKeyIdx).stringValue());
            }
        }
        System.out.println("SUCCESS: " + allRecords.size() + " Records Changed");
    }

    private void checkUniqueConstraint(int tableNum, List<Attribute> attrs, List<DataType> tuple) throws ExecutionFailure {
        List<List<DataType>> allRecords = null;
        for (int i = 0; i < attrs.size(); i++) {
            Attribute a = attrs.get(i);
            DataType value = tuple.get(i);

            int finalI = i;
            if (a.isUnique() && !a.isPrimaryKey()) {
                if (allRecords == null) {
                    allRecords = sm.getAllRecords(tableNum, attrs);
                }

                if (allRecords.stream().anyMatch(r -> r.get(finalI).compareTo(value) == 0))
                    throw new ExecutionFailure("Attribute '%s' is unique"
                            .formatted(a.getName()));
            }
        }
    }

    private List<DataType> convertStringToTuple(String entry, List<Attribute> attrs) throws ExecutionFailure {
        List<String> values = customSplit(entry);

        if (values.size() != attrs.size()) {
            throw new ExecutionFailure(UNEQUAL_ATTR_MSG.formatted(tableName, attrs.size(), values.size()));
        }

        List<DataType> tuple = new ArrayList<>();

        for (int i = 0; i < attrs.size(); i++) {

            DataType dt;

            Attribute a = attrs.get(i);
            String value = values.get(i);
            if (a.isNullable()) {
                value = value.equalsIgnoreCase("null") ? null : value;
            } else if (value.equalsIgnoreCase("null")) {
                throw new ExecutionFailure("Attribute '%s' is not nullable"
                        .formatted(a.getName()));
            }

            try {
                dt = switch (a.getDataType()) {
                    case INTEGER -> new DTInteger(value);
                    case DOUBLE -> new DTDouble(value);
                    case BOOLEAN -> new DTBoolean(value);
                    case CHAR -> new DTChar(value, a.getMaxDataLength());
                    case VARCHAR -> new DTVarchar(value);
                };
            } catch (NumberFormatException nfe) {
                throw new ExecutionFailure(INVALID_ATTR_TYPE_MSG.formatted(values.get(i), a.getName(), a.getDataType()));
            }

            tuple.add(dt);
        }

        return tuple;
    }

    private List<String> customSplit(String input) {
        List<String> result = new ArrayList<>();
        Matcher valueMatcher = VALUE_PATTERN.matcher(input);

        while (valueMatcher.find()) {
            result.add(valueMatcher.group());
        }

        return result;
    }

}
