package cli.cmd.commands;

import catalog.Attribute;
import catalog.ICatalog;
import catalog.NotSupportedConstraint;
import catalog.Table;
import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import cli.util.Format;
import dataTypes.*;
import sm.StorageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * <b>File:</b> Select.java
 * <p>
 * <b>Description: Command to select items from the database</b>
 *
 * @author Derek Garcia
 */
public class Select extends Command {

    private static final int MIN_WIDTH = 3;
    private static final int EXTRA_SPACES = 2;

    String tableName;

    public Select(String args) throws InvalidUsage {
        // Select String Syntax Validation
        String[] userInput = args.strip().split(" ");
        if(userInput.length != 4 || !userInput[1].equals("*") || !userInput[2].equalsIgnoreCase("from")){
            throw new InvalidUsage(args, "Correct Usage: (Select * From <table name>;)");
        }
        tableName = userInput[3].replace(";", "");
    }

    @Override
    protected void helpMessage() {
        // TODO
    }

    @Override
    public void execute(ICatalog catalog, StorageManager sm) throws ExecutionFailure {

        Set<String> allTables = catalog.getExistingTableNames();

        if (!allTables.contains(tableName)) {
            throw new ExecutionFailure("No such table %s".formatted(tableName));
        }

        int tableNum = catalog.getTableNumber(tableName);

        List<List<DataType>> allRecords = sm.getAllRecords(tableNum);

        Table table = catalog.getRecordSchema(tableName);
        List<Attribute> attrs = table.getAttributes();

        List<Integer> colWidths = getColumnWidths(attrs);

        System.out.println(createHeader(colWidths, attrs));
        System.out.println(createFormattedRows(colWidths, allRecords));
    }

    private List<Integer> getColumnWidths(List<Attribute> attrs) {
        List<Integer> widths = new ArrayList<>();

        for (Attribute attr : attrs) {
            switch (attr.getDataType()) {
                case AttributeType.BOOLEAN, AttributeType.DOUBLE, AttributeType.INTEGER ->
                        widths.add(Stream.of(attr.getName().length() + EXTRA_SPACES, MIN_WIDTH + EXTRA_SPACES)
                                .mapToInt(v -> v)
                                .max()
                                .orElseThrow(NoSuchElementException::new));
                case AttributeType.VARCHAR, AttributeType.CHAR -> {
                    try {
                        widths.add(Stream.of(attr.getMaxDataLength() + EXTRA_SPACES, attr.getName().length() + EXTRA_SPACES, MIN_WIDTH + EXTRA_SPACES)
                                .mapToInt(v -> v)
                                .max()
                                .orElseThrow(NoSuchElementException::new));
                    } catch (NotSupportedConstraint ignore) {}
                }
            }
        }

        return widths;
    }

    private String createHeader(List<Integer> colWidths, List<Attribute> attrs) {
        StringBuilder header = new StringBuilder("-");
        for (int w : colWidths) {
            header.repeat("-", w + 1);  // Add an extra for the vertical separator
        }
        header.append("\n|");
        for (int i = 0; i < colWidths.size(); i++) {
            header.append(Format.centerString(colWidths.get(i), attrs.get(i).getName()));
            header.append("|");
        }

        header.append("\n-");
        for (int w : colWidths) {
            header.repeat("-", w + 1);
        }

        return header.toString();
    }

    private String createFormattedRows(List<Integer> colWidths, List<List<DataType>> allRecords) {
        if (allRecords.isEmpty()) {
            return "";
        }

        StringBuilder rows = new StringBuilder();

        for (List<DataType> record : allRecords) {
            rows.append("\n");
            rows.append(createFormattedRow(colWidths, record));
        }

        return rows.deleteCharAt(0).toString();
    }

    private String createFormattedRow(List<Integer> colWidths, List<DataType> record) {

        StringBuilder row = new StringBuilder("|");

        for (int i = 0; i < colWidths.size(); i++) {
            row.append(Format.rightAlignString(colWidths.get(i), record.get(i).stringValue()));
            row.append("|");
        }

        return row.toString();
    }
}
