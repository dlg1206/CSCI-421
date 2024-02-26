package cli.cmd.commands;

import catalog.Attribute;
import catalog.ICatalog;
import catalog.Table;
import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import util.Console;
import util.Format;
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

    private final ICatalog catalog;
    private final StorageManager sm;

    private static final int MIN_WIDTH = 3;
    private static final int EXTRA_SPACES = 2;
    private static final String TABLE_DNE_MSG = "Table %s does not exist in the Catalog";

    private final String tableName;

    public Select(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {

        this.catalog = catalog;
        this.sm = storageManager;

        // Select String Syntax Validation
        List<String> input = getInput(args);
        if(input.size() != 4 || !input.get(1).equals("*") || !input.get(2).equalsIgnoreCase("from")){
            throw new InvalidUsage(args, "Correct Usage: (Select * From <table name>;)");
        }
        tableName = input.get(3).toLowerCase();

        Set<String> allTables = catalog.getExistingTableNames();

        if (!allTables.contains(tableName)) {
            throw new InvalidUsage(args, TABLE_DNE_MSG.formatted(tableName));
        }
    }

    @Override
    protected void helpMessage() {
        // TODO
    }

    @Override
    public void execute() throws ExecutionFailure {

        int tableNum = catalog.getTableNumber(tableName);

        List<List<DataType>> allRecords = sm.getAllRecords(tableNum, catalog.getRecordSchema(tableName).getAttributes());

        Table table = catalog.getRecordSchema(tableName);
        List<Attribute> attrs = table.getAttributes();

        List<Integer> colWidths = getColumnWidths(attrs);

        Console.out(createHeader(colWidths, attrs));
        Console.out(createFormattedRows(colWidths, allRecords));
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
                case AttributeType.VARCHAR, AttributeType.CHAR ->
                    widths.add(Stream.of(attr.getMaxDataLength() + EXTRA_SPACES, attr.getName().length() + EXTRA_SPACES, MIN_WIDTH + EXTRA_SPACES)
                            .mapToInt(v -> v)
                            .max()
                            .orElseThrow(NoSuchElementException::new));

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
