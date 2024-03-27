package util;

import java.util.List;

public class ReservedKeywords {

    public static final String INVALID_ATTR_NAME_MSG = "'%s' is a reserved keyword and cannot be used as an attribute name.";
    public static final String INVALID_TABLE_NAME_MSG = "'%s' is a reserved keyword and cannot be used as a table name.";

    private final static List<String> RESERVED_KEYWORDS = List.of(
            // Data types
            "Integer",
            "Boolean",
            "Double",
            "Char",
            "VarChar",
            // Commands
            "Create",
            "Insert",
            "Select",
            "Delete",
            "Update",
            "Drop",
            "Alter",
            "Display",
            // Constraints
            "primarykey",
            "unique",
            "notnull",
            // Utility words
            "Table",
            "Schema",
            "Set",
            "Where",
            "True",
            "False",
            "and",
            "or",
            "orderby",
            "From",
            "add",
            "default",
            "values",
            "info"
    );

    public static boolean isValidName(String name) {
        return RESERVED_KEYWORDS.stream().noneMatch(r -> r.equalsIgnoreCase(name));
    }

}
