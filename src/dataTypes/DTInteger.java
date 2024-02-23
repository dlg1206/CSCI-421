package dataTypes;

import java.nio.ByteBuffer;

public class DTInteger implements DataType, Comparable<DataType> {
    private Integer value;

    public DTInteger(byte[] byteValue) {
        if (byteValue == null)
            return;

        this.value = ByteBuffer.wrap(byteValue).getInt();
    }

    public DTInteger(String strValue) throws NumberFormatException {
        if (strValue == null)
            return;

        this.value = java.lang.Integer.parseInt(strValue);
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public byte[] convertToBytes() {
        return ByteBuffer.allocate(java.lang.Integer.BYTES).putInt(value).array();
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    public void printValue() {
        System.out.println("Integer: " + value);
    }

    @Override
    public String stringValue() {
        return this.isNull() ? "NULL" : value.toString();
    }

    public int compareTo(DataType o) {
        if (o.isNull() && this.isNull()) { return 0; } // both null
        else if (o.isNull() || this.isNull()) { return -1; } // one null
        else if (o instanceof DTInteger) { return ((DTInteger) o).value.compareTo(this.value); }
        return -1;
    }
}
