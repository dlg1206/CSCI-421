package util.where;

import dataTypes.AttributeType;

import java.util.regex.Matcher;

public abstract class Node {
    abstract boolean evaluate();

    abstract AttributeType getReturnType();
}
