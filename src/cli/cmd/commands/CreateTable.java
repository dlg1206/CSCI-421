package cli.cmd.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import catalog.Attribute;
import catalog.ICatalog;

import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;

import dataTypes.AttributeType;
import sm.StorageManager;

/**
 * <b>File:</b> CreateTable.java
 * <p>
 * <b>Description: Command to create a new table in the database</b>
 *
 * @author Derek Garcia, Clinten Hopkins
 */
public class CreateTable extends Command {

    private final ICatalog catalog;

    private final List<Attribute> attributes = new ArrayList<>();
    private final String tableName;

    private static final Pattern FULL_MATCH = Pattern.compile("create[\\s\\t]+table[\\s\\t]+([A-Za-z0-9]*)[\\s\\t]*\\([\\s\\t]*([a-z0-9()\\s\\t,]+)[\\s\\t]*\\)[\\s\\t]*;", Pattern.CASE_INSENSITIVE);
    private static final Pattern ATTR_MATCH = Pattern.compile("([a-z0-9]+)[\\s\\t]+([a-z0-9]+(?:\\([0-9]*\\))?)(?:[\\s\\t]+([a-z\\s\\t]*[a-z]))*", Pattern.CASE_INSENSITIVE);
    private static final Pattern ATTR_TYPE_SIZE_MATCH = Pattern.compile("([a-z0-9]+)(?:\\(([0-9]*)\\))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONSTRAINT_MATCH = Pattern.compile("([a-z]+)+", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-z][a-z0-9]*", Pattern.CASE_INSENSITIVE);

    public CreateTable(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {

        this.catalog = catalog;

        // Create Table Syntax Validation
        String errorMessage = "Correct Usage: create table <table_name> (" +
                            "<column_name> <data_type> [constraints], ...);";

        Matcher fullMatcher = FULL_MATCH.matcher(args);

        if(!fullMatcher.matches()){
            throw new InvalidUsage(args, errorMessage);
        }

        tableName = fullMatcher.group(1);

        Matcher tableNameMatcher = NAME_PATTERN.matcher(tableName);

        if (!tableNameMatcher.matches()) {
            throw new InvalidUsage(args, "The name '%s' is not a valid table name.".formatted(tableName));
        }

        Set<String> allTables = catalog.getExistingTableNames();
        if(allTables.contains(tableName)){
            throw new InvalidUsage(args, "Table " + tableName + " Already Exists");
        }

        Matcher attrMatcher = ATTR_MATCH.matcher(fullMatcher.group(2));

        while (attrMatcher.find()) {

            String attrName = attrMatcher.group(1);
            String attrType = attrMatcher.group(2);
            String attrConstraints = attrMatcher.group(3);

            Matcher attrNameMatcher = NAME_PATTERN.matcher(attrName);

            if (!attrNameMatcher.matches()) {
                throw new InvalidUsage(args, "The name '%s' is not a valid attribute name.".formatted(attrName));
            }

            if (attributes.stream().map(Attribute::getName).toList().contains(attrName)) {
                throw new InvalidUsage(args, "The attribute name '%s' cannot be used more than once.".formatted(attrName));
            }

            Matcher attrTypeSizeMatcher = ATTR_TYPE_SIZE_MATCH.matcher(attrType);

            if (!attrTypeSizeMatcher.matches()) {
                throw new InvalidUsage(args, "'%s' is not a valid attribute type".formatted(attrType));
            }

            AttributeType type;

            try {
                type = AttributeType.valueOf(attrTypeSizeMatcher.group(1).toUpperCase());
            } catch (IllegalArgumentException iae) {
                throw new InvalidUsage(args, "'%s' is not a valid attribute type.".formatted(attrType));
            }

            Integer maxDataLength = null;

            if (attrTypeSizeMatcher.group(2) != null) {
                switch (type) {
                    case INTEGER, DOUBLE, BOOLEAN ->
                            throw new InvalidUsage(args, "Attributes of type INTEGER, DOUBLE, or BOOLEAN do not require a max length.");
                    default -> {
                        try {
                            maxDataLength = Integer.parseInt(attrTypeSizeMatcher.group(2));
                        } catch (NumberFormatException nfe) {
                            throw new InvalidUsage(args, "The attribute's max length must be an integer.");
                        }
                    }
                }
            } else if (type == AttributeType.VARCHAR || type == AttributeType.CHAR) {
                throw new InvalidUsage(args, "Attributes of type CHAR or VARCHAR require a max length.");
            }

            boolean isNullable = true, isUnique = false, isPrimaryKey = false;

            if (attrConstraints != null) {
                Matcher constMatcher = CONSTRAINT_MATCH.matcher(attrConstraints);

                while (constMatcher.find()) {
                    String constraint = constMatcher.group();

                    System.out.println(constraint);

                    switch (constraint.toLowerCase()) {
                        case "notnull" -> isNullable = false;
                        case "unique" -> isUnique = true;
                        case "primarykey" -> isPrimaryKey = true;
                        default -> throw new InvalidUsage(args, "'%s' is not a valid constraint.".formatted(constraint));
                    }
                }
            }

            Attribute attr;

            switch (type) {
                case INTEGER, DOUBLE, BOOLEAN -> attr = isPrimaryKey ? new Attribute(attrName, type) :
                        new Attribute(attrName, type, isUnique, isNullable);
                default -> attr = isPrimaryKey ? new Attribute(attrName, type, maxDataLength) :
                        new Attribute(attrName, type, maxDataLength, isUnique, isNullable);
            }

            attributes.add(attr);

        }

        if (attributes.stream().filter(Attribute::isPrimaryKey).toList().size() > 1) {
            throw new InvalidUsage(args, "Only one attribute can be the primary key.");
        }
    }

    @Override
    protected void helpMessage() {
        // TODO
    }

    @Override
    public void execute() throws ExecutionFailure {
        try {
            catalog.createTable(tableName, attributes);
        } catch (IOException ioe) {
            throw new ExecutionFailure("The database could not access the required files to make this table.");
        }
    }
}
