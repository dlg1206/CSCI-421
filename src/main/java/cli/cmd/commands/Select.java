package cli.cmd.commands;

import catalog.Attribute;
import catalog.ICatalog;
import cli.cmd.exception.ExecutionFailure;
import cli.cmd.exception.InvalidUsage;
import util.Console;
import dataTypes.*;
import sm.StorageManager;
import util.where.WhereTree;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static util.Format.*;

/**
 * <b>File:</b> Select.java
 * <p>
 * <b>Description: Command to select items from the database</b>
 *
 * @author Derek Garcia
 */
public class Select extends Command {

    // CONSTANTS
    private static final String TABLE_DNE_MSG = "Table %s does not exist in the Catalog";
    private static final String BAD_ATTR_NAME_MSG = "The attribute names could not be parsed:";
    private static final Pattern TABLE_ATTR_PATTERN = Pattern.compile("([a-z][a-z0-9]*)(?:\\.([a-z][a-z0-9]*))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern FULL_SELECT_STMT = Pattern.compile("select\\s+(\\*|(?:[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)?(?:,\\s*)?)+)\\s+from\\s+((?:[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)?(?:,\\s*)?)+)(?:\\s+(where\\s+.+?))?(?:\\s+orderby\\s+(.+?))?\\s*;", Pattern.CASE_INSENSITIVE);

    // Base
    private final ICatalog catalog;
    private final StorageManager sm;

    private WhereTree whereTree = null;


    private final List<String> tableNames;
    private List<AttributeName> attrsToDisplay = null;

    private final String args;
    private AttributeName orderByData;


    //==================================================================================================================
    // Command Semantic/Syntactic Validation
    //==================================================================================================================

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
            attrsToDisplay = validateAttributeSet(List.of(FullMatch.group(1).split(",\\s*")),
                    getAllAttrs().stream().map(Attribute::getName).toList(),
                    getDistinctAttrNames());
        }

        String orderByArg = FullMatch.group(4);
        if (orderByArg != null) {
            orderByData = validateAttributeSet(List.of(FullMatch.group(4)),
                    getAllAttrs().stream().map(Attribute::getName).toList(),
                    getDistinctAttrNames()).getFirst();

            if (attrsToDisplay != null && attrsToDisplay.stream().noneMatch(a -> a.getFullName().equalsIgnoreCase(orderByData.getFullName()))) {
                throw new InvalidUsage(args, "The orderby attribute must be part of the projection in the select clause.");
            }
        }

    }

    @Override
    protected void helpMessage() {
        // TODO
    }

    //==================================================================================================================
    // Command Execution
    //==================================================================================================================

    @Override
    public void execute() throws ExecutionFailure {

        List<List<DataType>> goodRecords = getValidRecords();

        List<Attribute> finalAttrs = new ArrayList<>();
        List<List<DataType>> finalRecords = new ArrayList<>();
        for (int i = 0; i < goodRecords.size(); i++)
            finalRecords.add(new ArrayList<>());    // create a new array of empty lists with the same size as the records that passed the conditions
                                                    // this allows us to place the attributes in the order requested

        if (attrsToDisplay == null) {   // if there was no projection  (i.e. "select * ...")
            // then copy all the attribute names and for any that overlap, append the table name
            for (String t : tableNames) {
                for (Attribute a : catalog.getRecordSchema(t).getAttributes()) {
                    String displayName = a.getName();

                    if (!getDistinctAttrNames().containsKey(a.getName()))
                        displayName = "%s.%s".formatted(t, a.getName());

                    if (a.getDataType() == AttributeType.CHAR || a.getDataType() == AttributeType.VARCHAR)
                        finalAttrs.add(new Attribute(displayName, a.getDataType(), a.getMaxDataLength()));
                    else
                        finalAttrs.add(new Attribute(displayName, a.getDataType()));
                }
            }
            // and don't do any rearrangement of attributes in the records
            finalRecords = goodRecords;
        } else {    // if there was projection
            for (AttributeName attr : attrsToDisplay) {
                // copy each attribute, in order of projection, into the new array (renamed if ambiguous)
                int attrIdx = getTableAttrOffsets().get(attr.TableName) + catalog.getRecordSchema(attr.TableName).getIndexOfAttribute(attr.AttributeName);
                Attribute oldAttr = getAllAttrs().get(attrIdx);

                String displayName = getDistinctAttrNames().containsKey(oldAttr.getName()) ? attr.RequestedName : attr.getFullName();

                if (oldAttr.getDataType() == AttributeType.CHAR || oldAttr.getDataType() == AttributeType.VARCHAR)
                    finalAttrs.add(new Attribute(displayName, oldAttr.getDataType(), oldAttr.getMaxDataLength()));
                else
                    finalAttrs.add(new Attribute(displayName, oldAttr.getDataType()));

                // and copy over the attribute data from the records that passed comparison into the correct spot.
                for (int i = 0; i < goodRecords.size(); i++) {
                    finalRecords.get(i).add(goodRecords.get(i).get(attrIdx));
                }
            }
        }

        // Order if necessary (only by one attribute, and only ascending)
        if (orderByData != null) {
            int sortColIdx = attrsToDisplay != null
                ? attrsToDisplay.stream().map(AttributeName::getFullName).toList().indexOf(orderByData.getFullName())
                : getTableAttrOffsets().get(orderByData.TableName) + catalog.getRecordSchema(orderByData.TableName).getIndexOfAttribute(orderByData.AttributeName);


            finalRecords.sort((r1, r2) -> {
                return r2.get(sortColIdx).compareTo(r1.get(sortColIdx)); // reverse the order
            });
        }

        // Run the normal print routines
        List<Integer> colWidths = getColumnWidths(finalAttrs);
        Console.out(createHeader(colWidths, finalAttrs));
        Console.out(createFormattedRows(colWidths, finalRecords));
    }

    //==================================================================================================================
    // Attribute Name Validation
    //==================================================================================================================

    private List<AttributeName> validateAttributeSet(List<String> attrNames, List<String> AllAttrNames, Map<String, String> DistinctAttrNames) throws InvalidUsage {
        List<AttributeName> result = new ArrayList<>();
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

            AttributeName assembledName = new AttributeName(name, preDot, postDot);

            if (catalog.getRecordSchema(assembledName.TableName).getAttributes().stream().map(Attribute::getName)
                    .noneMatch(n -> n.equalsIgnoreCase(assembledName.AttributeName))) {
                parseErrors.add(assembledName.getFullName());
                parseErrors.add(" ".repeat(assembledName.TableName.length() + 1) +   // Extra 1 for the dot separator
                        "^".repeat(assembledName.AttributeName.length()) +
                        " This attribute does not exist in the table.");
                break;
            }

            result.add(assembledName);
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

    private static class AttributeName {
        String RequestedName;
        String TableName;
        String AttributeName;

        AttributeName(String requestedName, String tableName, String attributeName) {
            RequestedName = requestedName;
            TableName = tableName;
            AttributeName = attributeName;
        }

        String getFullName() {
            return "%s.%s".formatted(TableName, AttributeName);
        }
    }

    //==================================================================================================================
    // Record Collection and Comparison
    //==================================================================================================================

    private List<List<DataType>> getValidRecords() throws ExecutionFailure {
        List<List<DataType>> goodRecords = new ArrayList<>();

        if (tableNames.size() == 1) {
            String tName = tableNames.getFirst();
            int tableNum = catalog.getTableNumber(tName);
            if (whereTree != null) {
                goodRecords = sm.selectRecords(tableNum, catalog.getRecordSchema(tName).getAttributes(), whereTree);
            } else
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

        return goodRecords;
    }


    //==================================================================================================================
    // Table Data Creation
    //==================================================================================================================

    private Map<String, Integer> TableAttrOffsets;
    private Map<String, String> DistinctAttrNames;
    private List<Attribute> AllAttrs;

    private Map<String, Integer> getTableAttrOffsets() {
        if (TableAttrOffsets == null) {
            if (whereTree != null) {
                TableAttrOffsets = whereTree.TableAttrOffsets;
            } else {
                calculateTableAttrData();
            }
        }
        return TableAttrOffsets;
    }

    private Map<String, String> getDistinctAttrNames() {
        if (DistinctAttrNames == null) {
            if (whereTree != null) {
                DistinctAttrNames = whereTree.DistinctAttrNames;
            } else {
                calculateTableAttrData();
            }
        }
        return DistinctAttrNames;
    }

    private List<Attribute> getAllAttrs() {
        if (AllAttrs == null)
            calculateTableAttrData();
        return AllAttrs;
    }

    private void calculateTableAttrData() {
        int totalAttrCount = 0;
        Map<String, List<String>> attrNameCounts = new HashMap<>();
        TableAttrOffsets = new HashMap<>();
        DistinctAttrNames = new HashMap<>();
        AllAttrs = new ArrayList<>();


        for (String tName : tableNames) {
            TableAttrOffsets.put(tName, totalAttrCount);
            for (Attribute a : catalog.getRecordSchema(tName).getAttributes()) {
                AllAttrs.add(a);
                totalAttrCount++;
                if (!attrNameCounts.containsKey(a.getName())) {
                    attrNameCounts.put(a.getName(), new ArrayList<>());
                }
                attrNameCounts.get(a.getName()).add(tName);
            }
        }

        attrNameCounts.forEach((k, v) -> {
            if (v.size() == 1)
                DistinctAttrNames.put(k, v.getFirst());
        });
    }
}
