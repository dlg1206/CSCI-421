package util.where;

import catalog.Attribute;
import catalog.ICatalog;
import cli.cmd.exception.ExecutionFailure;
import dataTypes.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhereTree {

    private final ICatalog Catalog;
    private final List<String> TableNames;
    public final Map<String, Integer> TableAttrOffsets = new HashMap<>();
    public final Map<String, String> DistinctAttrNames = new HashMap<>();
    public final List<String> AllAttrNames = new ArrayList<>();
    private InternalNode tree;

    private String UnparsedContent;

    private final List<String> parseErrors = new ArrayList<>();
    private static final String BAD_WHERE_MSG = "The where clause is invalid:";

    private static final Pattern GLOBAL_PATTERN = Pattern.compile("where\\s+(.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ALGEBRA_BRANCH_PATTERN = Pattern.compile("((?:\".*?\"|[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)?|[0-9]+\\.[0-9]*|[0-9]+)\\s*(?:>|<|=|!=|<=|>=)\\s*(?:\".*?\"|[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)?|[0-9]+\\.[0-9]*|[0-9]+))(?:\\s+(.*))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONDITIONAL_BRANCH_PATTERN = Pattern.compile("(and|or)\\s+(.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern LEAF_NODE_PATTERN = Pattern.compile("(\".*?\"|[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)?|[0-9]+\\.[0-9]*|[0-9]+)\\s*(>|<|=|!=|<=|>=)\\s*(\".*?\"|[a-z][a-z0-9]*(?:\\.[a-z][a-z0-9]*)?|[0-9]+\\.[0-9]*|[0-9]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TABLE_ATTR_PATTERN = Pattern.compile("([a-z][a-z0-9]*)(?:\\.([a-z][a-z0-9]*))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUOTED_STRING_PATTERN = Pattern.compile("\"(.*)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("[0-9]+\\.[0-9]*", Pattern.CASE_INSENSITIVE);
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);

    public WhereTree(String input, ICatalog catalog, List<String> tableNames) throws ExecutionFailure {
        Catalog = catalog;
        TableNames = tableNames;

        Map<String, List<String>> attrNameCounts = new HashMap<>();

        int totalAttrCount = 0;

        for (String tName : TableNames) {
            TableAttrOffsets.put(tName, totalAttrCount);
            for (Attribute a : Catalog.getRecordSchema(tName).getAttributes()) {
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
            AllAttrNames.add(k);
        });

        parseInput(input);
        if (!isValidTree()){
            StringBuilder sb = new StringBuilder(BAD_WHERE_MSG).append("\n");

            for (String e : parseErrors) {
                sb.append("\t").append(e).append("\n");
            }

            throw new ExecutionFailure(sb.toString());
        }
    }

    private void parseInput(String input) {
        Matcher overallMatcher = GLOBAL_PATTERN.matcher(input);

        if (!overallMatcher.matches()) {
            parseErrors.add("The format did not match the expected \"where <condition>;\"");
            return;
        }

        UnparsedContent = overallMatcher.group(1);
        tree = recursiveParse();

        while (UnparsedContent != null) {
            if (!parseErrors.isEmpty())
                return;
            tree = new InternalNode(tree, recursiveParse(), "or");
        }
    }

    private InternalNode recursiveParse() {
        Matcher innerLeafMatcher = ALGEBRA_BRANCH_PATTERN.matcher(UnparsedContent);

        if (!innerLeafMatcher.matches()) {
            parseErrors.add(UnparsedContent);
            parseErrors.add("^ Parse error. The parser could not validate this relational algebra.");
            return null;
        }

        String leafString = innerLeafMatcher.group(1);

        Matcher leafMatcher = LEAF_NODE_PATTERN.matcher(leafString);

        if (!leafMatcher.matches()) {
            // I don't expect this error to throw, but its just there as a sanity check.
            // We have to run matches for our groups, so might as well double check and throw if needed.
            parseErrors.add(leafString);
            parseErrors.add("^ Parse error. The parser could not validate this relational algebra.");
            return null;
        }

        String left = leafMatcher.group(1);
        LeafNode leftLeaf = createLeaf(left);
        if (leftLeaf == null)
            return null;


        String comparer = leafMatcher.group(2);
        String right = leafMatcher.group(3);
        LeafNode rightLeaf = createLeaf(right);
        if (rightLeaf == null)
            return null;

        InternalNode inner = new InternalNode(leftLeaf, rightLeaf, comparer);

        UnparsedContent = innerLeafMatcher.group(2);

        if (UnparsedContent == null)
            return inner;

        Matcher conditionalMatcher = CONDITIONAL_BRANCH_PATTERN.matcher(UnparsedContent);

        if (!conditionalMatcher.matches()) {
            parseErrors.add(UnparsedContent);
            parseErrors.add("^ Parse error. Tokens \"and\" or \"or\" expected here.");
            return null;
        }

        boolean andCondition = conditionalMatcher.group(1).equals("and");
        UnparsedContent = conditionalMatcher.group(2);

        if (andCondition) {
            return new InternalNode(inner, recursiveParse(), "and");
        } else {
            return inner;
        }
    }

    private boolean isValidTree() {
        if (!parseErrors.isEmpty()) {
            return false; // if there was a parsing issue, don't even try to validate.
        }

        return validateSubtree(tree);
    }

    private boolean validateSubtree(Node node) {
        if (node instanceof LeafNode leaf) {
            if (leaf.Attribute == null)
                return true;
            if (leaf.TableName == null) {
                if (!AllAttrNames.contains(leaf.Attribute)) {
                    parseErrors.add(leaf.Attribute);
                    parseErrors.add("^ This attribute is not part of any of the requested tables.");
                    return false;
                } else if (!DistinctAttrNames.containsKey(leaf.Attribute)) {
                    parseErrors.add(leaf.Attribute);
                    parseErrors.add("^ This attribute name is ambiguous between multiple tables.");
                    return false;
                } else {
                    leaf.TableName = DistinctAttrNames.get(leaf.Attribute);
                }
            }
            if (!TableNames.contains(leaf.TableName)) {
                parseErrors.add(leaf.TableName);
                parseErrors.add("^ This table name does not exist.");
                return false;
            }

            if (Catalog.getRecordSchema(leaf.TableName).getAttributes().stream().map(Attribute::getName)
                    .noneMatch(n -> n.equals(leaf.Attribute))) {
                parseErrors.add(leaf.toString());
                parseErrors.add(" ".repeat(leaf.TableName.length() + 1) +   // Extra 1 for the dot separator
                        "^".repeat(leaf.Attribute.length()) +
                        " This attribute does not exist in the table.");
                return false;
            }

            leaf.ReturnType = Catalog.getTableAttribute(leaf.TableName, leaf.Attribute).getDataType();
            leaf.TableNum = Catalog.getTableNumber(leaf.TableName);
            return true;
        } else if (node instanceof InternalNode internal) {
            boolean validChildren = validateSubtree(internal.Left) && validateSubtree(internal.Right);

            if (!validChildren)
                return false;

            boolean matchingTypes = internal.Left.getReturnType().equals(internal.Right.getReturnType());

            if (!matchingTypes) {
                if (internal.Left instanceof LeafNode lLeaf && internal.Right instanceof LeafNode rLeaf){
                    if (lLeaf.getReturnType() == AttributeType.VARCHAR && rLeaf.getReturnType() == AttributeType.CHAR && rLeaf.Value != null) {
                        rLeaf.Value = new DTVarchar(rLeaf.Value.stringValue());
                        rLeaf.ReturnType = AttributeType.VARCHAR;
                        return true;
                    } else if (rLeaf.getReturnType() == AttributeType.VARCHAR && lLeaf.getReturnType() == AttributeType.CHAR && lLeaf.Value != null) {
                        lLeaf.Value = new DTVarchar(lLeaf.Value.stringValue());
                        lLeaf.ReturnType = AttributeType.VARCHAR;
                        return true;
                    }
                }

                parseErrors.add(internal.toString());
                parseErrors.add("^".repeat(internal.Left.toString().length()) +
                        " ".repeat(internal.Comparator.length() + 2) +
                        "^".repeat(internal.Right.toString().length()) +
                        " The return types of these two expressions are not comparable ( " + internal.Left.getReturnType().name() +
                        " and " + internal.Right.getReturnType().name() + " ).");
                return false;
            }
            return true;
        }
        parseErrors.add("Something bad happened. A tree node was found that didn't match a Leaf or Internal node.");
        return false;
    }

    public boolean passesTree(List<DataType> record) {
        return passesSubtree(tree, record);
    }

    private boolean passesSubtree(Node node, List<DataType> record) {
        if (!(node instanceof InternalNode iNode))
            return false; // This should never happen, the recursion should never pass in a leaf node here.

        Predicate<Integer> comparator =
            switch (iNode.Comparator) {
                case ">" ->  t -> t < 0; // TODO: This feels so wrong, someone should check if this is right.
                case ">="  -> t -> t <= 0;
                case "<" -> t -> t > 0;
                case "<=" -> t -> t >= 0;
                case "=" -> t -> t == 0;
                case "!=" -> t -> t != 0;
                default -> t -> t == Integer.MAX_VALUE; // This will be ignored when the Comparator is "and" or "or"
            };



        if (iNode.Left instanceof LeafNode lLeaf && iNode.Right instanceof LeafNode rLeaf) {
            DataType lValue;
            if (lLeaf.Value != null)
                lValue = lLeaf.Value;
            else {
                lValue = record.get(Catalog.getRecordSchema(lLeaf.TableName).getIndexOfAttribute(lLeaf.Attribute) +
                        TableAttrOffsets.get(lLeaf.TableName));
            }
            DataType rValue;
            if (rLeaf.Value != null)
                rValue = rLeaf.Value;
            else {
                rValue = record.get(Catalog.getRecordSchema(rLeaf.TableName).getIndexOfAttribute(rLeaf.Attribute) +
                        TableAttrOffsets.get(rLeaf.TableName));
            }
            return comparator.test(lValue.compareTo(rValue));
        }

        if (iNode.Comparator.equalsIgnoreCase("and"))
            return passesSubtree(iNode.Left, record) && passesSubtree(iNode.Right, record);
        else
            return passesSubtree(iNode.Left, record) || passesSubtree(iNode.Right, record);
    }

    private LeafNode createLeaf(String value) {
        LeafNode node;

        Matcher leftTableMatcher = TABLE_ATTR_PATTERN.matcher(value);
        Matcher stringMatcher = QUOTED_STRING_PATTERN.matcher(value);
        Matcher doubleMatcher = DOUBLE_PATTERN.matcher(value);
        Matcher booleanMatcher = BOOLEAN_PATTERN.matcher(value);
        if (booleanMatcher.matches()) {
            node = new LeafNode(Boolean.parseBoolean(booleanMatcher.group()));
        } else if (leftTableMatcher.matches()){
            String preDot = leftTableMatcher.group(1);
            String postDot = leftTableMatcher.group(2);
            if (postDot == null)
                node = new LeafNode(null, preDot);
            else
                node = new LeafNode(preDot, postDot);
        } else if (stringMatcher.matches()) {
            node = new LeafNode(stringMatcher.group(1));
        } else if (doubleMatcher.matches()) {
            node = new LeafNode(Double.parseDouble(doubleMatcher.group()));
        } else {
            try {
                node = new LeafNode(Integer.parseInt(value));
            } catch (NumberFormatException nfe) {
                parseErrors.add(value);
                parseErrors.add("^ Parse error. This value could not be parsed as a leaf node.");
                return null;
            }
        }
        return node;
    }

    @Override
    public String toString() {
        return tree.toString();
    }
}
