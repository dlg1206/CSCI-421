package cli.cmd.commands;

import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;

import dataTypes.AttributeType;
import sm.StorageManager;

import catalog.ICatalog;
import catalog.Table;
import catalog.Attribute;

import java.util.List;
import java.util.Set;

/**
 * <b>File:</b> AlterTable.java
 * <p>
 * <b>Description: Command to alter a table in the database</b>
 *
 * @author Derek Garcia
 */
public class AlterTable extends Command {

    private final ICatalog catalog;
    private final StorageManager sm;

    private final String tableName;
    private final String attributeName;
    private AttributeType newType = null;
    private Integer maxDataLength = null;

    public AlterTable(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {

        this.catalog = catalog;
        this.sm = storageManager;

        // Alter Table Syntax Validation
        List<String> input = getInput(args);
        String errorMessage = """
                Correct Usage: (alter table <name> drop <a_name>;
                 alter table <name> add <a_name> <a_type>;
                 alter table <name> add <a_name> <a_type> default <value>);""";

        if (input.size() < 5 || !input.get(1).equalsIgnoreCase("table")) {
            throw new InvalidUsage(args, errorMessage);
        }

        boolean isValid = switch (input.size()) {
            case 5 -> input.get(3).equalsIgnoreCase("drop");
            case 6 -> input.get(3).equalsIgnoreCase("add");
            case 8 -> input.get(3).equalsIgnoreCase("add") && input.get(6).equalsIgnoreCase("default");
            default -> false;
        };

        if (!isValid) {
            throw new InvalidUsage(args, errorMessage);
        }

        tableName = input.get(2);


        // Alter Table Semantic Validation
        Set<String> allTables = catalog.getExistingTableNames();
        if(!allTables.contains(tableName)){
            throw new InvalidUsage(args, "Table " + tableName + " does not Exist in the Catalog");
        }

        Table table = catalog.getRecordSchema(tableName);
        List<Attribute> attributes = table.getAttributes();

        attributeName = input.get(4).toLowerCase();
        List<String> tableAttributeNames = attributes.stream().map(a -> a.getName().toLowerCase()).toList();
        if (input.get(3).equalsIgnoreCase("drop")){
            if (!tableAttributeNames.contains(attributeName)){
                throw new InvalidUsage(args, "The table '%s' does not contain the attribute '%s'.".formatted(tableName, attributeName));
            }
        }
        else {
            if (tableAttributeNames.contains(attributeName)){
                throw new InvalidUsage(args, "The table '%s' already contains the attribute '%s'.".formatted(tableName, attributeName));
            }

            String[] newAttributeData = input.get(5).split("\\(");
            String typeString = newAttributeData[0].toUpperCase();

            try {
                newType = AttributeType.valueOf(typeString);
            } catch (IllegalArgumentException iae) {
                throw new InvalidUsage(args, "'%s' is not a valid attribute type.".formatted(typeString));
            }

            switch (newType) {
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
                        maxDataLength = Integer.parseInt(maxLengthString.substring(1, maxLengthString.length() - 1));
                    } catch (NumberFormatException nfe) {
                        throw new InvalidUsage(args, "The attribute's max length must be an integer.");
                    }
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
        // TODO
    }
}
