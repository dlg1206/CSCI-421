package cli.cmd.commands;

import cli.cmd.commands.CreateTable;
import cli.cmd.commands.DropTable;

import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;

import dataTypes.AttributeType;
import dataTypes.DTChar;
import dataTypes.DTVarchar;
import dataTypes.DataType;
import sm.StorageManager;

import catalog.ICatalog;
import catalog.Table;
import catalog.Attribute;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import util.Console;

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
    private final String args;

    private boolean isDrop = false;
    private boolean isAdd = false;


    private final String tableName;
    private final String attributeName;
    private AttributeType newType = null;
    private Integer maxDataLength = null;

    public AlterTable(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {

        this.catalog = catalog;
        this.sm = storageManager;
        this.args = args;

        // Alter Table Syntax Validation
        List<String> input = getInput(args);
        String errorMessage = """
                Correct Usage: (alter table <name> drop <a_name>;
                 alter table <name> add <a_name> <a_type>;
                 alter table <name> add <a_name> <a_type> default <value>;)""";

        if (input.size() < 5 || !input.get(1).equalsIgnoreCase("table")) {
            throw new InvalidUsage(args, errorMessage);
        }
        boolean isValid = false;
        switch (input.size()) {
            case 5:
                isValid = input.get(3).equalsIgnoreCase("drop");
                this.isDrop = true;
                break;
            case 6:
                isValid = input.get(3).equalsIgnoreCase("add");
                this.isAdd = true;
                break;
            case 8 :
                isValid = input.get(3).equalsIgnoreCase("add") && input.get(6).equalsIgnoreCase("default");
                this.isAdd = true;
                break;
            default: 
                isValid = false;
        };

        if (!isValid) {
            throw new InvalidUsage(args, errorMessage);
        }

        tableName = input.get(2).toLowerCase();


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
                    String maxLengthString = newAttributeData[1].replace(")","");
                    try {
                        maxDataLength = Integer.parseInt(maxLengthString);
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
        // Check if the command is dropping an attribute
        int tableNumber = catalog.getTableNumber(tableName);
        List<List<DataType>> allRecordsList = sm.getAllRecords(tableNumber, catalog.getRecordSchema(tableName).getAttributes());
        if(isDrop){
            int attributeIndex = 0;
            Table table = catalog.getRecordSchema(tableName);
            List<Attribute> attributes = table.getAttributes();
            // Iterate through all attributes from schema and drop the desired attribute and keep track of its index
            for (int i = 0; i < attributes.size(); i++) {
                if(attributes.get(i).getName().equalsIgnoreCase(attributeName)){
                    if(attributes.get(i).isPrimaryKey()){
                        throw new ExecutionFailure("Execution failure cannot drop primary key");
                    }
                    else{
                        attributes.remove(i);
                        attributeIndex = i;
                    }
                }
            }
            // Creating Create Table Command
            String attributeValues = "";
            // Iterate through all attributes from schema
            for (int v = 0; v < attributes.size(); v++){
                // Get attribute and stringify all its respective information
                Attribute attr = attributes.get(v);
                // If last value then dont add a comma, otherwise have a comma
                // Result should look like 'AttName AttType AttConstraint, AttName AttType AttConstraint'
                if(v == attributes.size()-1){
                    attributeValues = attributeValues + attr.getName() + " " + getStringType(attr) + getConstraintString(attr);
                }
                else{
                    attributeValues = attributeValues + attr.getName() + " " + getStringType(attr) + getConstraintString(attr) + ", ";
                }   
            }
            // Creating Insert Into Command
            String insertValues = "";
            // Iterate through first list
            for (int r = 0; r < allRecordsList.size(); r++) {
                List<DataType> record = allRecordsList.get(r);
                String eachValue = " ( ";
                // Iterate through each value
                for (int i = 0; i < record.size(); i++) {
                    // If it is a char or varchar then add quotation marks
                    if(!(i == attributeIndex)){
                        String recordString = record.get(i).stringValue();
                    if(record.get(i) instanceof DTChar || record.get(i) instanceof DTVarchar){
                        eachValue = eachValue + "\"" + recordString + "\"" + " ";
                    }
                    else{
                        eachValue = eachValue +  recordString  + " ";
                    }
                    }
                }
                if(r == allRecordsList.size()-1){
                    eachValue += ")";
                }
                else{
                    eachValue += " ),";
                }
                // Add each ( Value, Value, Value ), or ( Value, Value, Value ) to main string
                // Result should look like ( Value, Value, Value ), ( Value, Value, Value ), ( Value, Value, Value )
                insertValues += eachValue;
            }
            // Create each command
            String dropTableCommand ="DROP TABLE " + tableName + ";".strip();
            String createTableCommand = "CREATE TABLE " + tableName + "( " + attributeValues + " );".strip();
            String insertIntoCommand = "INSERT INTO " + tableName + " VALUES" + insertValues + ";".strip();
            try {
                // Run each command
                DropTable newDropTableExecutable = new DropTable(dropTableCommand, this.catalog, this.sm);
                newDropTableExecutable.execute();
                CreateTable newTableExecutable = new CreateTable(createTableCommand, this.catalog, this.sm);
                newTableExecutable.execute();
                if(!(allRecordsList.size() == 0)){
                    InsertInto newInsertIntoExecutable = new InsertInto(insertIntoCommand, this.catalog, this.sm);
                    newInsertIntoExecutable.execute();
                }
                else{
                    Console.out("SUCCESS");
                }
            } catch (InvalidUsage e) {
                throw new ExecutionFailure("Execution failure to drop attribute");
            }
        }
        else if(isAdd){
            // Get attribute type from command line argument
            List<String> input = getInput(args);
            String attributeTypeString = input.get(5);
            // Get default value from command line argument
            String defaultValue = "";
            if(this.args.toLowerCase().contains("default")){
                String[] takeDefault = args.toLowerCase().split("default");
                defaultValue = takeDefault[1].replace(";", "").strip();
            }
            Table table = catalog.getRecordSchema(tableName);
            List<Attribute> attributes = table.getAttributes();
            // Creating Create Table Command
            String attributeValues = "";
            // Iterate through all attributes from schema
            for (int v = 0; v < attributes.size(); v++){
                // Get attribute and stringify all its respective information
                Attribute attr = attributes.get(v);
                attributeValues = attributeValues + attr.getName() + " " + getStringType(attr) + getConstraintString(attr) + ", ";
            }
            // Result should look like 'AttName AttType AttConstraint, AttName AttType AttConstraint, NewAttributeName NewAttributeType'
            attributeValues += attributeName + " " + attributeTypeString;
            // Creating Insert Into Command
            String insertValues = "";
            // Iterate through first list
            for (int r = 0; r < allRecordsList.size(); r++) {
                List<DataType> record = allRecordsList.get(r);
                String eachValue = " ( ";
                // Iterate through each value
                for (int i = 0; i < record.size(); i++) {
                    // If it is a char or varchar then add quotation marks
                    if(record.get(i) instanceof DTChar || record.get(i) instanceof DTVarchar){
                        eachValue = eachValue + "\"" + record.get(i).stringValue() + "\"" + " ";
                    }
                    else{
                        eachValue += record.get(i).stringValue() + " ";
                    }
                }
                // If argument contained default, then use default value, otherwise input null value
                if(r == allRecordsList.size()-1){
                    eachValue += args.toLowerCase().contains("default") ? " " + defaultValue + " )" : " null )";
                }
                else{
                    eachValue += args.toLowerCase().contains("default") ? " " + defaultValue + " )," : " null ), ";
                }
                // Add each ( Value, Value, defaultValue ), or ( Value, Value, null ) to main string
                // Result should look like ( Value, Value, null ), ( Value, Value, null ), ( Value, Value, null )
                insertValues += eachValue;
            }
            // Create each command
            String dropTableCommand ="DROP TABLE " + tableName + ";".strip();
            String createTableCommand = "CREATE TABLE " + tableName + "( " + attributeValues + " );".strip();
            String insertIntoCommand = "INSERT INTO " + tableName + " VALUES" + insertValues + ";".strip();
            try {
                // Run each command
                DropTable newDropTableExecutable = new DropTable(dropTableCommand, this.catalog, this.sm);
                newDropTableExecutable.execute();
                CreateTable newTableExecutable = new CreateTable(createTableCommand, this.catalog, this.sm);
                newTableExecutable.execute();
                if(!(allRecordsList.size() == 0)){
                    InsertInto newInsertIntoExecutable = new InsertInto(insertIntoCommand, this.catalog, this.sm);
                    newInsertIntoExecutable.execute();
                }
                else{
                    Console.out("SUCCESS");
                }
                
            } catch (InvalidUsage e) {
                throw new ExecutionFailure("Execution failure to add attribute");
            }
        }
    }

    public String getConstraintString(Attribute attr){
        String temp = "";
        if(attr.isPrimaryKey()){
            temp = temp + " primarykey";
        }
        else{
            if(attr.isUnique()){
            temp = temp + " unique";
        }
        if(!attr.isNullable()){
            temp = temp + " notnull";
        }
        }
        
        return temp;
    }

    public String getStringType(Attribute attr) {
        AttributeType attrType = attr.getDataType();
        switch (attrType) {
            case INTEGER -> {
                return "integer";
            }
            case DOUBLE -> {
                return "double";
            }
            case BOOLEAN -> {
                return "boolean";
            }
            case CHAR -> {
                return "char" + "(" + attr.getMaxDataLength() + ")";
            }
            default -> {
                return "varchar" + "(" + attr.getMaxDataLength() + ")";
            }
        }

    }
}
