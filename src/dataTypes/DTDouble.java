package dataTypes;

import util.Console;

import java.nio.ByteBuffer;

public class DTDouble implements DataType, Comparable<DataType> {
    private Double value;

    public DTDouble(byte[] byteValue) {
        if (byteValue == null)
            return;

        this.value = ByteBuffer.wrap(byteValue).getDouble();
    }

    public DTDouble(String strValue) throws NumberFormatException {
        if (strValue == null)
            return;

        this.value = java.lang.Double.parseDouble(strValue);
    }

    public Double getValue() {
        return value;
    }

    @Override
    public byte[] convertToBytes() {
        return ByteBuffer.allocate(java.lang.Double.BYTES).putDouble(value).array();
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    public void printValue() {
        Console.out("Double: " + value);
    }

    @Override
    public String stringValue() {
        return this.isNull() ? "NULL" : value.toString();
    }

    public int compareTo(DataType o) {
        if (o.isNull() && this.isNull()) { return 0; } // both null
        else if (o.isNull() || this.isNull()) { return -1; } // one null
        else if (o instanceof DTDouble) { return ((DTDouble) o).value.compareTo(this.value); }
        return -1;
    }
}

