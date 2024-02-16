package cli.cmd.commands;

import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import dataTypes.AttributeType;
import dataTypes.DTBoolean;
import dataTypes.DTChar;
import dataTypes.DTDouble;
import dataTypes.DTInteger;
import dataTypes.DTVarchar;
import catalog.Catalog;
import catalog.Attribute;
import catalog.Table;

import dataTypes.DTInteger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;

/**
 * <b>File:</b> InsertInto.java
 * <p>
 * <b>Description: Command to insert data into a table in the database<</b>
 *
 * @author Derek Garcia
 */
public class InsertInto extends Command {

    private Catalog catalog;
    private Map<Integer, List<String>> mappedValues = new HashMap<>();
    private Map<Integer, List<AttributeType>> mappedAttTypeValues = new HashMap<>();
    

    public InsertInto(String args) throws InvalidUsage {
        // Insert Into Syntax Validation
        if (!args.contains("values")) {
            throw new InvalidUsage(args, "Correct Usage: (insert into <name> values <tuples>);");
        }
        
        String[] values = args.strip().split("values");
        if (values.length != 2 || values[1].isEmpty()) {
            throw new InvalidUsage(args, "Correct Usage: (insert into <name> values <tuples>);");
        }
        
        String[] input1 = values[0].strip().split(" ");
        if (input1.length != 3 || !input1[1].equalsIgnoreCase("into")) {
            throw new InvalidUsage(args, "Correct Usage: (insert into <name> values <tuples>);");
        }
        String tableName = input1[2];
        String valuesString = values[1].replace(";", " ").replace(")", "");

        // Tales all values and puts them in a hashmap
        if(!valuesString.contains(",")){
            List<String> tokens = splitStringWithQuotes(valuesString);
            mappedValues.put(0, tokens);
        }
        else{
            String[] multipleValues =valuesString.split(",");
            for (int i = 0; i < multipleValues.length; i++) {
                List<String> tokens = splitStringWithQuotes(multipleValues[i]);
                mappedValues.put(i, tokens);
            }
        }
        // Insert Into Semantical Validation
        if(!catalog.getExistingTableNames().contains(tableName)){
            throw new InvalidUsage(args, "This Table Does Not Exist in the Catalog");
        }
        Table table = catalog.getRecordSchema(tableName);
        List<Attribute> attributes = table.getAttributes();
        for (int i = 0; i < mappedValues.size(); i++) {
            List<String> lst = mappedValues.get(i);
            for (int next = 0; next < lst.size(); next++) {
                String stringVal = lst.get(next);
                
                AttributeType att = attributes.get(next).getDataType();
                Object result;

                switch (att) {
                    case INTEGER -> result = new DTInteger(stringVal);
                    case DOUBLE -> result = new DTDouble(stringVal);
                    case BOOLEAN -> result = new DTBoolean(stringVal);
                    case CHAR -> result = new DTChar(stringVal);
                    case VARCHAR -> result = new DTVarchar(stringVal);


                }

            }

        }

    }

    public static List<String> splitStringWithQuotes(String input) {
        List<String> tokens = new ArrayList<>();
        // The updated pattern below will now correctly handle the "3" and "\"baz\"" case
        Pattern pattern = Pattern.compile("(?<=\\))|(?=\\()|\"[^\"]*\"|(\\S+?)(?=(\\s|$))");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String match = matcher.group();
            if (!match.isEmpty()) { // avoid adding empty strings due to the lookaround matches at the start and end
                tokens.add(match);
            }
        }

        // Post-processing to handle cases like 3"baz" 
        List<String> processedTokens = new ArrayList<>();
        for (String token : tokens) {
            if (token.matches("\\d+\"[^\"]*\"")) { // matches a number directly followed by a quoted string
                // Split into number and quoted string
                Matcher numberQuotedStringMatcher = Pattern.compile("(\\d+)(\"[^\"]*\")").matcher(token);
                if (numberQuotedStringMatcher.find()) {
                    processedTokens.add(numberQuotedStringMatcher.group(1)); // number part
                    processedTokens.add(numberQuotedStringMatcher.group(2)); // quoted string part
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