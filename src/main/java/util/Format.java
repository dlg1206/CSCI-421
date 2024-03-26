package util;

import catalog.Attribute;
import dataTypes.AttributeType;
import dataTypes.DataType;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public class Format {

    private static final int MIN_WIDTH = 3;
    private static final int EXTRA_SPACES = 2;

    public static String centerString (int width, String s) {
        return String.format("%-" + width  + "s", String.format("%" + (s.length() + (width - s.length()) / 2) + "s", s));
    }

    public static String rightAlignString (int width, String s) {
        return String.format("%" + width  + "." + width + "s", s);
    }

    public static List<Integer> getColumnWidths(List<Attribute> attrs) {
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

    public static String createHeader(List<Integer> colWidths, List<Attribute> attrs) {
        StringBuilder header = new StringBuilder("-");
        for (int w : colWidths) {
            header.repeat("-", w + 1);  // Add an extra for the vertical separator
        }
        header.append("\n|");
        for (int i = 0; i < colWidths.size(); i++) {
            header.append(centerString(colWidths.get(i), attrs.get(i).getName()));
            header.append("|");
        }

        header.append("\n-");
        for (int w : colWidths) {
            header.repeat("-", w + 1);
        }

        return header.toString();
    }

    public static String createFormattedRows(List<Integer> colWidths, List<List<DataType>> allRecords) {
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

    private static String createFormattedRow(List<Integer> colWidths, List<DataType> record) {

        StringBuilder row = new StringBuilder("|");

        for (int i = 0; i < colWidths.size(); i++) {
            row.append(Format.rightAlignString(colWidths.get(i), record.get(i).stringValue()));
            row.append("|");
        }

        return row.toString();
    }
}
