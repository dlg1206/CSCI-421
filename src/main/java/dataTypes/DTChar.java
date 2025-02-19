package dataTypes;

import java.util.Objects;

import util.Console;

public class DTChar implements DataType, Comparable<DataType> {
    private String value;
    private int maxLength;

    private final String padding = "\0";

    public DTChar(byte[] byteValue, int maxLength) {
        if (byteValue == null)
            return;

        // replace removes any padding that was added when writing to hardware
        this.value = new String(byteValue).replace(padding, "");
        this.maxLength = maxLength;
    }

    public DTChar(String strValue, int maxLength) {
        this.value = strValue;
        this.maxLength = maxLength;
    }

    public String getValue() {
        return value;
    }

    @Override
    public byte[] convertToBytes() {
        // add padding to reach max length
        int paddingLen = maxLength - value.length();
        return (value + padding.repeat(paddingLen)).getBytes();
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    public void printValue() {
        Console.out("Char: " + value);
    }

    @Override
    public String stringValue() {
        return this.isNull() ? "NULL" : value;
    }

    @Override
    public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    DTChar dtChar = (DTChar) obj;
    return Objects.equals(value, dtChar.value);
    }  

    public int compareTo(DataType o) {
        if (o.isNull() && this.isNull()) { return 0; } // both null
        else if (o.isNull() || this.isNull()) { return -1; } // one null
        else if (o instanceof DTChar) { return ((DTChar) o).value.compareTo(this.value); }
        return -1;
    }
}

