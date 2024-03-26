package cli.cmd.commands;

import catalog.Attribute;
import catalog.ICatalog;
import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import util.Console;
import util.Format;
import dataTypes.*;
import sm.StorageManager;
import util.where.WhereTree;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private WhereTree whereTree = null;

    private static final int MIN_WIDTH = 3;
    private static final int EXTRA_SPACES = 2;
    private static final String TABLE_DNE_MSG = "Table %s does not exist in the Catalog";
    private static final String BAD_ATTR_NAME_MSG = "The attribute names could not be parsed:";
    private static final Pattern TABLE_ATTR_PATTERN = Pattern.compile("([a-z][a-z0-9]*)(?:\\.([a-z][a-z0-9]*))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern FULL_SELECT_STMT = Pattern.compile("select\\s+(\\*|(?:[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)?(?:,\\s*)?)+)\\s+from\\s+((?:[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)?(?:,\\s*)?)+)(?:\\s+(where\\s+.+?))?(?:\\s+orderby\\s+(.+?))?\\s*;", Pattern.CASE_INSENSITIVE);

    private final List<String> tableNames;
    private List<String> attrsToDisplay = null;

    private final String args;
    private String[] orderByData;

    public Select(String args, ICatalog catalog, StorageManager storageManager) throws InvalidUsage {

        this.catalog = catalog;
        this.sm = storageManager;
        this.args = args;

        // Select String Syntax Validation
        Matcher FullMatch = FULL_SELECT_STMT.matcher(args);

        if (!FullMatch.matches())
            throw new InvalidUsage(args, "Correct Usage: (select <a_1>, ..., <a_N> from <t_1>, ..., <t_N> [where <condition(s)>] [orderby <a_1>];)");

        tableNames = List.of(FullMatch.group(2).split(",\\s*"));

        Set<String> allTables = catalog.getExistingTableNames();

        for (String tName : tableNames){
            if (!allTables.contains(tName.toLowerCase())) {
                throw new InvalidUsage(args, TABLE_DNE_MSG.formatted(tName));
            }
        }

        if (FullMatch.group(3) != null) {
            try {
                whereTree = new WhereTree(FullMatch.group(3), catalog, tableNames);
            } catch (ExecutionFailure ef) {
                throw new InvalidUsage(args, ef.getMessage());
            }
        }


        if (!FullMatch.group(1).equals("*")){
            if (whereTree != null)
                attrsToDisplay = validateAttributeSet(List.of(FullMatch.group(1).split(",\\s*")), whereTree);
            else
                attrsToDisplay = validateAttributeSet(List.of(FullMatch.group(1).split(",\\s*")));
        }

        String orderByArg = FullMatch.group(4);
        if (orderByArg != null) {
            if (whereTree != null)
                orderByData = validateAttributeSet(List.of(FullMatch.group(4)), whereTree).getFirst().split("\\.");
            else
                orderByData = validateAttributeSet(List.of(FullMatch.group(4))).getFirst().split("\\.");
        }

        if (attrsToDisplay != null && attrsToDisplay.stream().noneMatch(a -> a.equalsIgnoreCase("%s.%s".formatted(orderByData[0], orderByData[1])))) {
            throw new InvalidUsage(args, "The orderby attribute must be part of the projection in the select clause.");
        }
    }

    @Override
    protected void helpMessage() {
        // TODO
    }

    @Override
    public void execute() throws ExecutionFailure {

        int totalAttrCount = 0;
        Map<String, List<String>> attrNameCounts = new HashMap<>();
        Map<String, Integer> TableAttrOffsets = new HashMap<>();
        Map<String, String> distinctAttrNames = new HashMap<>();
        List<Attribute> allAttrs = new ArrayList<>();


        for (String tName : tableNames) {
            TableAttrOffsets.put(tName, totalAttrCount);
            for (Attribute a : catalog.getRecordSchema(tName).getAttributes()) {
                allAttrs.add(a);
                totalAttrCount++;
                if (!attrNameCounts.containsKey(a.getName())) {
                    attrNameCounts.put(a.getName(), new ArrayList<>());
                }
                attrNameCounts.get(a.getName()).add(tName);
            }
        }

        attrNameCounts.forEach((k, v) -> {
            if (v.size() == 1)
                distinctAttrNames.put(k, v.getFirst());
        });

        List<List<DataType>> goodRecords = new ArrayList<>();

        if (tableNames.size() == 1) {
            String tName = tableNames.getFirst();
            int tableNum = catalog.getTableNumber(tName);
            if (whereTree != null)
                goodRecords = sm.selectRecords(tableNum, catalog.getRecordSchema(tName).getAttributes(), whereTree);
            else
                goodRecords = sm.getAllRecords(tableNum, catalog.getRecordSchema(tName).getAttributes());
        }
        else {
            List<List<DataType>> cartesianProduct = new ArrayList<>();

            for (String tName : tableNames) {
                int tableNum = catalog.getTableNumber(tName);
                List<List<DataType>> allRecords;

                if (whereTree != null && whereTree.TableOptimizations.containsKey(tName)) {
                    WhereTree optimizingTree = whereTree.TableOptimizations.get(tName);
                    allRecords = sm.selectRecords(tableNum, catalog.getRecordSchema(tName).getAttributes(), optimizingTree);
                }
                else
                    allRecords = sm.getAllRecords(tableNum, catalog.getRecordSchema(tName).getAttributes());

                if (cartesianProduct.isEmpty()) {
                    cartesianProduct = allRecords;
                    continue;
                }

                List<List<DataType>> newCProduct = new ArrayList<>();
                for (List<DataType> cRecord : cartesianProduct) {
                    for (List<DataType> nRecord : allRecords) {
                        List<DataType> joined = new ArrayList<>(cRecord);
                        joined.addAll(nRecord);
                        newCProduct.add(joined);
                    }
                }
                cartesianProduct = newCProduct;
            }

            for (List<DataType> record : cartesianProduct) {
                if (whereTree == null || whereTree.passesTree(record))
                    goodRecords.add(record);
            }
        }

        List<Attribute> finalAttrs = new ArrayList<>();
        List<List<DataType>> finalRecords = new ArrayList<>();
        for (int i = 0; i < goodRecords.size(); i++)
            finalRecords.add(new ArrayList<>());

        if (attrsToDisplay == null) {
            for (String t : tableNames)
                for (Attribute a : catalog.getRecordSchema(t).getAttributes()) {
                    if (distinctAttrNames.containsKey(a.getName())) {
                        finalAttrs.add(a);
                    }
                    else {
                        String newName = "%s.%s".formatted(t, a.getName());
                        if (a.getDataType() == AttributeType.CHAR || a.getDataType() == AttributeType.VARCHAR)
                            finalAttrs.add(new Attribute(newName, a.getDataType(), a.getMaxDataLength()));
                        else
                            finalAttrs.add(new Attribute(newName, a.getDataType()));
                    }
                }
            finalRecords = goodRecords;
        } else {
            for (String attr : attrsToDisplay) {
                String[] attrData = attr.split("\\.");  // attrData[0] is table name, attrData[1] is attr name

                int attrIdx = TableAttrOffsets.get(attrData[0]) + catalog.getRecordSchema(attrData[0]).getIndexOfAttribute(attrData[1]);
                Attribute oldAttr = allAttrs.get(attrIdx);
                if (distinctAttrNames.containsKey(oldAttr.getName())) {
                    finalAttrs.add(oldAttr);
                }
                else {
                    String newName = "%s.%s".formatted(attrData[0], attrData[1]);
                    if (oldAttr.getDataType() == AttributeType.CHAR || oldAttr.getDataType() == AttributeType.VARCHAR)
                        finalAttrs.add(new Attribute(newName, oldAttr.getDataType(), oldAttr.getMaxDataLength()));
                    else
                        finalAttrs.add(new Attribute(newName, oldAttr.getDataType()));
                }

                for (int i = 0; i < goodRecords.size(); i++) {
                    finalRecords.get(i).add(goodRecords.get(i).get(attrIdx));
                }
            }
        }

        // TODO Implement orderby on the finalRecords list
        if (orderByData != null) {
            int sortColIdx = TableAttrOffsets.get(orderByData[0]) + catalog.getRecordSchema(orderByData[0]).getIndexOfAttribute(orderByData[1]);

            finalRecords.sort((r1, r2) -> {
                return r2.get(sortColIdx).compareTo(r1.get(sortColIdx)); // reverse the order
            });
        }

        List<Integer> colWidths = getColumnWidths(finalAttrs);

        Console.out(createHeader(colWidths, finalAttrs));
        Console.out(createFormattedRows(colWidths, finalRecords));
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

    private List<String> validateAttributeSet(List<String> attrNames, WhereTree whereTree) throws InvalidUsage {
        return validateAttributeSet(attrNames, whereTree.AllAttrNames, whereTree.DistinctAttrNames);
    }

    private List<String> validateAttributeSet(List<String> attrNames) throws InvalidUsage {

        Map<String, List<String>> attrNameCounts = new HashMap<>();

        Map<String, String> DistinctAttrNames = new HashMap<>();
        List<String> AllAttrNames = new ArrayList<>();

        for (String tName : tableNames) {
            for (Attribute a : catalog.getRecordSchema(tName).getAttributes()) {
                if (!attrNameCounts.containsKey(a.getName())) {
                    attrNameCounts.put(a.getName(), new ArrayList<>());
                }
                attrNameCounts.get(a.getName()).add(tName);
            }
        }

        attrNameCounts.forEach((k, v) -> {
            if (v.size() == 1)
                DistinctAttrNames.put(k, v.getFirst());
            AllAttrNames.add(k);
        });

        return validateAttributeSet(attrNames, AllAttrNames, DistinctAttrNames);
    }

    private List<String> validateAttributeSet(List<String> attrNames, List<String> AllAttrNames, Map<String, String> DistinctAttrNames) throws InvalidUsage {
        List<String> result = new ArrayList<>();
        List<String> parseErrors = new ArrayList<>();
        for (String name : attrNames) {

            Matcher attrNameMatcher = TABLE_ATTR_PATTERN.matcher(name);
            if (!attrNameMatcher.matches())
                throw new InvalidUsage(args, "The attribute %s is not parsable.".formatted(name));
            String preDot = attrNameMatcher.group(1);
            String postDot = attrNameMatcher.group(2);

            if (postDot == null) {
                if (!AllAttrNames.contains(preDot)) {
                    parseErrors.add(preDot);
                    parseErrors.add("^ This attribute is not part of any of the requested tables.");
                    break;
                } else if (!DistinctAttrNames.containsKey(preDot)) {
                    parseErrors.add(preDot);
                    parseErrors.add("^ This attribute name is ambiguous between multiple tables.");
                    break;
                } else {
                    postDot = preDot;
                    preDot = DistinctAttrNames.get(preDot);
                }
            }
            if (!tableNames.contains(preDot)) {
                parseErrors.add(preDot);
                parseErrors.add("^ This table name does not exist.");
                break;
            }

            String fullName = "%s.%s".formatted(preDot, postDot);

            String attrName = postDot; // postDot needs to be final for the lambda to work. This doesn't actually do anything.

            if (catalog.getRecordSchema(preDot).getAttributes().stream().map(Attribute::getName)
                    .noneMatch(n -> n.equalsIgnoreCase(attrName))) {
                parseErrors.add(fullName);
                parseErrors.add(" ".repeat(preDot.length() + 1) +   // Extra 1 for the dot separator
                        "^".repeat(postDot.length()) +
                        " This attribute does not exist in the table.");
                break;
            }

            result.add(fullName);
        }

        if (!parseErrors.isEmpty()) {
            StringBuilder sb = new StringBuilder(BAD_ATTR_NAME_MSG).append("\n");

            for (String e : parseErrors) {
                sb.append("\t").append(e).append("\n");
            }

            throw new InvalidUsage(args, sb.toString());
        }

        return result;
    }
}
