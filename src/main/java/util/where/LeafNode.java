package util.where;

import dataTypes.*;

import java.util.Objects;

public class LeafNode extends Node{
    String TableName;
    String Attribute;
    Integer TableNum;
    DataType Value;
    AttributeType ReturnType;

    LeafNode(String table, String attribute){
        TableName = table;
        Attribute = attribute;
    }

    LeafNode(String value){
        Value = new DTChar(value, value.length());
        ReturnType = AttributeType.CHAR;
    }

    LeafNode(int value){
        Value = new DTInteger(Objects.toString(value));
        ReturnType = AttributeType.INTEGER;
    }

    LeafNode(double value){
        Value = new DTDouble(Objects.toString(value));
        ReturnType = AttributeType.DOUBLE;
    }

    LeafNode(boolean value){
        Value = new DTBoolean(Objects.toString(value));
        ReturnType = AttributeType.BOOLEAN;
    }

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public AttributeType getReturnType() {
        return ReturnType;
    }

    @Override
    public String toString() {
        if (Value != null)
            return (Value instanceof DTChar || Value instanceof DTVarchar)
                    ? "\"" + Value.stringValue() + "\""
                    : Value.stringValue();
        else
        if (TableName == null)
            return Attribute;
        else
            return TableName + "." + Attribute;
    }
}
