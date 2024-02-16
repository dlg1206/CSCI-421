package cli.cmd.commands;

import catalog.Attribute;
import catalog.Catalog;
import catalog.Table;
import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;

import catalog.Catalog;

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

    private Catalog catalog;

    public AlterTable(String args) throws InvalidUsage {
        // Alter Table Syntax Validation
        String[] input = args.strip().split(" ");
        String errorMessage = "Correct Usage: (alter table <name> drop <a_name>;" +
                            "\n alter table <name> add <a_name> <a_type>;" +
                            "\n alter table <name> add <a_name> <a_type> default <value>);";

        if (input.length < 5 || !input[1].equalsIgnoreCase("table")) {
            throw new InvalidUsage(args, errorMessage);
        }

        boolean isValid = switch (input.length) {
            case 5 -> input[3].equalsIgnoreCase("drop");
            case 6 -> input[3].equalsIgnoreCase("add");
            default -> input[3].equalsIgnoreCase("add") && input[6].equalsIgnoreCase("default");
        };

        if (!isValid) {
            throw new InvalidUsage(args, errorMessage);
        }

        String[] tempName = args.toLowerCase().split("table");
        String tableName = tempName[1].split(" ")[1];
        System.out.println(tableName);

        // Alter Table Semantic Validation
        Set<String> allTables = catalog.getExistingTableNames();
        if(!allTables.contains(tableName)){
            throw new InvalidUsage(args, "Table " + tableName + " does not Exist in the Catalog");
        }

        Table table = catalog.getRecordSchema(tableName);
        List<Attribute> attributes = table.getAttributes();

        if(args.contains("drop")){
            tempName = args.split("drop");
            String attributeName = tempName[0];
            Boolean checkExist = true;
            for (Attribute att : attributes) {
                if(att.getName().equals(attributeName)){
                    checkExist = false;
                }
            }
            if(checkExist){
                throw new InvalidUsage(args, "This Table does not Contain the Attribute: " + tableName);
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
