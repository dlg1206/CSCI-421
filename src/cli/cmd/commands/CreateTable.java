package cli.cmd.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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

    public CreateTable(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {

        this.catalog = catalog;

        // Create Table Syntax Validation
        String errorMessage = "Correct Usage: create table <table_name> (" +
                            "<column_name> <data_type> [constraints], ...);";
        if(!args.contains("(")){
            throw new InvalidUsage(args, errorMessage);
        }
        int indexOfOpen = args.indexOf("(");
        String firstPart = args.substring(0, indexOfOpen);
        String secondPart = args.substring(indexOfOpen + 1);
        
        String[] command = firstPart.split(" ");
        if (!command[1].equalsIgnoreCase("table") || command.length != 3) {
            throw new InvalidUsage(args, errorMessage);
        }

        if (!Pattern.compile("\\)\\s*;").matcher(secondPart).find()) {
            throw new InvalidUsage(args, errorMessage);
        }
        String[] columns = secondPart.strip().substring(0, secondPart.lastIndexOf(")")-1).split(",");
        if(columns.length < 1){
            throw new InvalidUsage(args, errorMessage);
        }

        // Create Table Semantical Validation
        String [] tempLst = args.toLowerCase().split("table");
        tableName = tempLst[1].substring(0, tempLst[1].indexOf("(")).strip();

        Set<String> allTables = catalog.getExistingTableNames();
        if(allTables.contains(tableName)){
            throw new InvalidUsage(args, "Table " + tableName + " Already Exists");
        }

        for (String col : columns) {
            String[] attrData = col.strip().split(" ");

            String attrName = attrData[0].strip();

            if (!attrName.matches("[a-zA-Z]+[a-zA-Z0-9]*")) {
                throw new InvalidUsage(args, "The string '%s' is not a valid attribute name.".formatted(attrName));
            }

            if (attributes.stream().map(Attribute::getName).toList().contains(attrName)) {
                throw new InvalidUsage(args, "The attribute name '%s' cannot be used more than once.".formatted(attrName));
            }

            if (attrData.length < 2 || attrData.length > 5) {
                throw new InvalidUsage(args, errorMessage);
            }

            String[] newAttributeData = attrData[1].split("\\(");
            String typeString = newAttributeData[0].toUpperCase();

            AttributeType type;

            Integer maxDataLength = null;

            try {
                type = AttributeType.valueOf(typeString);
            } catch (IllegalArgumentException iae) {
                throw new InvalidUsage(args, "'%s' is not a valid attribute type.".formatted(typeString));
            }

            switch (type) {
                case INTEGER, DOUBLE, BOOLEAN -> {
                    if (newAttributeData.length != 1) {
                        throw new InvalidUsage(args, "Attributes of type INTEGER, DOUBLE, or BOOLEAN do not require a max length.");
                    }
                }
                default -> {
                    if (newAttributeData.length != 2) {
                        throw new InvalidUsage(args, "Attributes of type CHAR or VARCHAR require a max length.");
                    }
                    String maxLengthString = newAttributeData[1];
                    try {
                        maxDataLength = Integer.parseInt(maxLengthString.substring(0, maxLengthString.length() - 1));
                    } catch (NumberFormatException nfe) {
                        throw new InvalidUsage(args, "The attribute's max length must be an integer.");
                    }
                }
            }

            boolean isNullable = true, isUnique = false, isPrimaryKey = false;


            if (attrData.length > 2) {
                for (String constraint : Arrays.stream(attrData).toList().subList(2,attrData.length)) {
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
                case INTEGER, DOUBLE, BOOLEAN -> attr = isPrimaryKey ? new Attribute(attrData[0], type) :
                        new Attribute(attrData[0].strip(), type, isUnique, isNullable);
                default -> attr = isPrimaryKey ? new Attribute(attrData[0], type, maxDataLength) :
                        new Attribute(attrData[0].strip(), type, maxDataLength, isUnique, isNullable);
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
        catalog.createTable(tableName, attributes);
    }
}
