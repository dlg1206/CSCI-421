package cli.cmd.commands;

import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;

import dataTypes.*;

import catalog.ICatalog;
import catalog.Attribute;
import catalog.Table;

import sm.StorageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>File:</b> InsertInto.java
 * <p>
 * <b>Description: Command to insert data into a table in the database<</b>
 *
 * @author Derek Garcia
 */
public class InsertInto extends Command {

    private final ICatalog catalog;
    private final StorageManager sm;
    private final List<List<DataType>> parsedValues = new ArrayList<>();

    private static final String CORRECT_USAGE_MSG = "Correct Usage: (insert into <name> values <tuples>);";
    private static final String TABLE_DNE_MSG = "Table %s does not exist in the Catalog";
    private static final String UNEQUAL_ATTR_MSG = "Table %s expects %s attributes and you provided %s for tuple #%s";
    private static final String INVALID_ATTR_TYPE_MSG = "The provided value '%s' for attribute '%s' is not of type %s for tuple #%s.";
    private static final String INVALID_ATTR_LENGTH_MSG = "The attribute '%s' has a max length of %s characters. You provided too many characters in tuple #%s";
    private static final String NO_QUOTES_MSG = "The attribute '%s' takes a string, which must be wrapped in quotes. You did not do this for tuple #%s";


    public InsertInto(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {

        this.catalog = catalog;
        this.sm = storageManager;

        // Insert Into Syntax Validation
        if (!args.contains("values")) {
            throw new InvalidUsage(args, CORRECT_USAGE_MSG);
        }
        
        String[] values = args.strip().split("values");
        if (values.length != 2 || values[1].isEmpty()) {
            throw new InvalidUsage(args, CORRECT_USAGE_MSG);
        }
        
        String[] input1 = values[0].strip().split("\\s+");
        if (input1.length != 3 || !input1[1].equalsIgnoreCase("into")) {
            throw new InvalidUsage(args, CORRECT_USAGE_MSG);
        }
        String tableName = input1[2];
        String valuesString = values[1].replace(";", " ").replace(")", "");

        // Takes all values and puts them in a list where each element is its own tuple
        List<List<String>> tupleValues = new ArrayList<>();
        if(!valuesString.contains(",")){
            List<String> tokens = splitStringWithQuotes(valuesString);
            tupleValues.add(tokens);
        }
        else{
            String[] multipleValues =valuesString.split(",");
            for (String multipleValue : multipleValues) {
                List<String> tokens = splitStringWithQuotes(multipleValue);
                tupleValues.add(tokens);
            }
        }
        // Insert Into Semantical Validation
        if(!catalog.getExistingTableNames().contains(tableName)){
            throw new InvalidUsage(args, TABLE_DNE_MSG.formatted(tableName));
        }

        Table table = catalog.getRecordSchema(tableName);
        List<Attribute> attributes = table.getAttributes();

        for (int i = 0; i < tupleValues.size(); i++) {
            List<String> lst = tupleValues.get(i);

            List<DataType> parsed = new ArrayList<>();

            if (lst.size() != attributes.size()) {
                throw new InvalidUsage(args,
                        UNEQUAL_ATTR_MSG.formatted(tableName, attributes.size(), lst.size(), i + 1));
            }

            for (int next = 0; next < lst.size(); next++) {
                String stringVal = lst.get(next);

                Attribute attr = attributes.get(next);
                AttributeType attrType = attributes.get(next).getDataType();
                DataType result = null;

                try {
                    switch (attrType) {
                        case INTEGER -> result = new DTInteger(stringVal);
                        case DOUBLE -> result = new DTDouble(stringVal);
                        case BOOLEAN -> result = new DTBoolean(stringVal);
                        case CHAR -> {
                            if (!stringVal.startsWith("\"") || !stringVal.endsWith("\"")) {
                                throw new InvalidUsage(args,
                                        NO_QUOTES_MSG.formatted(attr.getName(), i));
                            }
                            stringVal = stringVal.substring(1, stringVal.length() - 1);
                            if (stringVal.length() > attr.getMaxDataLength()) {
                                throw new InvalidUsage(args,
                                        INVALID_ATTR_LENGTH_MSG.formatted(attr.getName(), attr.getMaxDataLength(), i));
                            }
                            result = new DTChar(stringVal);
                        }
                        case VARCHAR -> {
                            if (!stringVal.startsWith("\"") || !stringVal.endsWith("\"")) {
                                throw new InvalidUsage(args,
                                        NO_QUOTES_MSG.formatted(attr.getName(), i));
                            }
                            stringVal = stringVal.substring(1, stringVal.length() - 1);
                            if (stringVal.length() > attr.getMaxDataLength()) {
                                throw new InvalidUsage(args,
                                        INVALID_ATTR_LENGTH_MSG.formatted(attr.getName(), attr.getMaxDataLength(), i));
                            }
                            result = new DTVarchar(stringVal);
                        }
                    }

                    parsed.add(result);
                } catch (NumberFormatException nfe) {
                    throw new InvalidUsage(args,
                            INVALID_ATTR_TYPE_MSG.formatted(stringVal, attr.getName(), attrType.name(), i));
                }
            }

            parsedValues.add(parsed);
        }
    }

    public static List<String> splitStringWithQuotes(String input) {
        List<String> tokens = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?<=\\))|(?=\\()|\"[^\"]*\"|(\\S+?)(?=(\\s|$))");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String match = matcher.group();
            if (!match.isEmpty()) { 
                tokens.add(match);
            }
        }

        // Post-processing to handle cases like 3"baz"
        List<String> processedTokens = new ArrayList<>();
        for (String token : tokens) {
            if (token.matches("\\d+\"[^\"]*\"")) { 
                // Split into number and quoted string
                Matcher numberQuotedStringMatcher = Pattern.compile("(\\d+)(\"[^\"]*\")").matcher(token);
                if (numberQuotedStringMatcher.find()) {
                    processedTokens.add(numberQuotedStringMatcher.group(1)); 
                    processedTokens.add(numberQuotedStringMatcher.group(2)); 
                }
            } else {
                processedTokens.add(token);
            }
        }

        return processedTokens;
    }

    @Override
    protected void helpMessage() {
        // TODO
    }

    @Override
    public void execute() throws ExecutionFailure {
        // TODO
    }
}