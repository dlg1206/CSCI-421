package cli.cmd.commands;

import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;

import dataTypes.*;

import catalog.ICatalog;
import catalog.Attribute;

import sm.StorageManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <b>File:</b> InsertInto.java
 * <p>
 * <b>Description:</b> Command to insert data into a table in the database.
 *
 * @author Derek Garcia, Clinten Hopkins
 */
public class InsertInto extends Command {

    private final ICatalog catalog;
    private final StorageManager sm;
    private final List<String> tuples = new ArrayList<>();

    private final String tableName;

    private static final String CORRECT_USAGE_MSG = "Correct Usage: (insert into <name> values <tuples>);";
    private static final String TABLE_DNE_MSG = "Table %s does not exist in the Catalog";
    private static final String UNEQUAL_ATTR_MSG = "Table %s expects %s attributes and you provided %s for tuple #%s";
    private static final String INVALID_ATTR_TYPE_MSG = "The provided value '%s' for attribute '%s' is not of type %s for tuple #%s.";
    private static final String INVALID_ATTR_LENGTH_MSG = "The attribute '%s' has a max length of %s characters. You provided too many characters in tuple #%s";
    private static final String NO_QUOTES_MSG = "The attribute '%s' takes a string, which must be wrapped in quotes. You did not do this for tuple #%s";

    private static final Pattern FULL_PATTERN = Pattern.compile("insert[\\s\\t]+into[\\s\\t]+([a-z0-9]*)[\\s\\t]+values[\\s\\t]+((?:\\([0-9\\s\"a-z.]+\\),*[\\s\\t]*)+);", Pattern.CASE_INSENSITIVE);
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("[a-z][a-z0-9]*", Pattern.CASE_INSENSITIVE);
    private static final Pattern TUPLE_PATTERN = Pattern.compile("\\(\\s*([0-9\\s\"a-z.]+)\\s*\\)", Pattern.CASE_INSENSITIVE);
    private static final Pattern STRING_PATTERN = Pattern.compile("\"(.+)\"", Pattern.CASE_INSENSITIVE);

    /**
     * Create a new Insert Into command to be executed. Parse the arguments to allow
     * {@link InsertInto#execute() execute} to operate.
     *
     * @param args The string representation of the command passed to the CLI.
     * @param catalog The catalog of the current DB.
     * @param storageManager The storage manager of the current DB.
     * @throws InvalidUsage when the arguments could not be parsed.
     */
    public InsertInto(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {

        this.catalog = catalog;
        this.sm = storageManager;


        Matcher fullMatcher = FULL_PATTERN.matcher(args);
        // Insert Into Syntax Validation
        if (!fullMatcher.matches()) {
            throw new InvalidUsage(args, CORRECT_USAGE_MSG);
        }
        tableName = fullMatcher.group(1);

        Matcher tableNameMatcher = TABLE_NAME_PATTERN.matcher(tableName);
        if (!tableNameMatcher.matches()) {
            throw new InvalidUsage(args, "The name '%s' is not a valid table name.".formatted(tableName));
        }

        if (catalog.getRecordSchema(tableName) == null) {
            throw new InvalidUsage(args, TABLE_DNE_MSG.formatted(tableName));
        }

        String allTuples = fullMatcher.group(2);

        Matcher tupleMatcher = TUPLE_PATTERN.matcher(allTuples);

        while (tupleMatcher.find()) {
            tuples.add(tupleMatcher.group(1));
        }

    }

    @Override
    protected void helpMessage() {
        // TODO
    }

    /**
     * Iterates through the list of parsed values pulled from the command and inserts them into the database through
     * the storage manager.
     *
     * @throws ExecutionFailure when the table's file cannot be read or modified.
     */
    @Override
    public void execute() throws ExecutionFailure {
        int tableNumber = catalog.getTableNumber(tableName);
        List<Attribute> attrs = catalog.getRecordSchema(tableName).getAttributes();
        int PKIndex = catalog.getRecordSchema(tableName).getIndexOfPrimaryKey();


        for (int i = 0; i < tuples.size(); i++) {
            List<DataType> tuple = convertStringToTuple(tuples.get(i), attrs, i);
            try {

                if (sm.getAllRecords(tableNumber, attrs).stream().anyMatch(r -> r.get(PKIndex).compareTo(tuple.get(PKIndex)) == 0))
                    throw new ExecutionFailure("There already exists an entry for primary key: '%s'.".formatted(tuple.get(PKIndex).stringValue()));

                checkUniqueConstraint(tableNumber, attrs, tuple, i);

                sm.insertRecord(tableNumber, attrs, tuple);

            } catch (IOException ioe) {
                throw new ExecutionFailure("The file for the table '%s' could not be opened or modified.".formatted(tableName));
            }

        }
        System.out.println("SUCCESS");
    }

    private void checkUniqueConstraint(int tableNum, List<Attribute> attrs, List<DataType> tuple, int tupleNum) throws ExecutionFailure {
        for (int i = 0; i < attrs.size(); i++) {
            Attribute a = attrs.get(i);
            DataType value = tuple.get(i);

            int finalI = i;
            if (a.isUnique() && sm.getAllRecords(tableNum, attrs).stream().anyMatch(r -> r.get(finalI).compareTo(value) == 0)) {
                throw new ExecutionFailure("Attribute '%s' is unique, you violate this constraint in tuple #%s"
                        .formatted(a.getName(), tupleNum));
            }
        }
    }

    private List<DataType> convertStringToTuple(String entry, List<Attribute> attrs, int tupleNum) throws ExecutionFailure {
        List<String> values = List.of(entry.split("\\s+"));

        if (values.size() != attrs.size()) {
            throw new ExecutionFailure(UNEQUAL_ATTR_MSG.formatted(tableName, attrs.size(), values.size(), tupleNum));
        }

        List<DataType> tuple = new ArrayList<>();

        for (int i = 0; i < attrs.size(); i++) {

            DataType dt;

            Attribute a = attrs.get(i);
            String value = values.get(i);
            if (a.isNullable()) {
                value = value.equalsIgnoreCase("null") ? null : value;
            } else if (value.equalsIgnoreCase("null")) {
                throw new ExecutionFailure("Attribute '%s' is not nullable, you violate this constraint in tuple #%s"
                        .formatted(a.getName(), tupleNum));
            }

            if ((a.getDataType() == AttributeType.CHAR || a.getDataType() == AttributeType.VARCHAR) && value != null) {
                Matcher stringMatcher = STRING_PATTERN.matcher(value);

                if (!stringMatcher.matches()) {
                    throw new ExecutionFailure(NO_QUOTES_MSG.formatted(a.getName(), tupleNum));
                }

                value = stringMatcher.group(1);

                if (value.length() > a.getMaxDataLength()) {
                    throw new ExecutionFailure(INVALID_ATTR_LENGTH_MSG.formatted(a.getName(), a.getMaxDataLength(), tupleNum));
                }
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
                throw new ExecutionFailure(INVALID_ATTR_TYPE_MSG.formatted(values.get(i), a.getName(), a.getDataType(), tupleNum));
            }

            tuple.add(dt);
        }

        return tuple;
    }
}