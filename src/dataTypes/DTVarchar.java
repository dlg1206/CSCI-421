package dataTypes;

import cli.util.Console;

public class DTVarchar implements DataType, Comparable<DataType> {
    private String value;

    public DTVarchar(byte[] byteValue) {
        if (byteValue == null)
            return;

        this.value = new String(byteValue);
    }

    public DTVarchar(String strValue) {
        this.value = strValue;
    }

    public String getValue() {
        return value;
    }

    @Override
    public byte[] convertToBytes() {
        return value.getBytes();
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    public void printValue() {
        Console.out("Varchar: " + value);
    }

    @Override
    public String stringValue() {
        return this.isNull() ? "NULL" : value;
    }

    public int compareTo(DataType o) {
        if (o.isNull() && this.isNull()) { return 0; } // both null
        else if (o.isNull() || this.isNull()) { return -1; } // one null
        else if (o instanceof DTVarchar) { return ((DTVarchar) o).value.compareTo(this.value); }
        return -1;
    }
}
