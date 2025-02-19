package dataTypes;

import java.util.Objects;

import util.Console;

public class DTBoolean implements DataType, Comparable<DataType> {
    private Boolean value;

    public DTBoolean(byte[] byteValue) {
        if (byteValue == null)
            return;

        this.value = byteValue[0] != 0;
    }

    public DTBoolean(String strValue) {
        if (strValue == null)
            return;

        this.value = java.lang.Boolean.parseBoolean(strValue);
    }

    public Boolean getValue() {
        return value;
    }

    @Override
    public byte[] convertToBytes() {
        return new byte[] {(byte) (value?1:0)};
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    public void printValue() {
        Console.out("Boolean: " + value);
    }

    @Override
    public String stringValue() {
        return this.isNull() ? "NULL" : (value ? "T" : "F");
    }

    @Override
    public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    DTBoolean dtBoolean = (DTBoolean) obj;
    return Objects.equals(value, dtBoolean.value);
    }   
  
    public int compareTo(DataType o) {
        if (o.isNull() && this.isNull()) { return 0; } // both null
        else if (o.isNull() || this.isNull()) { return -1; } // one null
        else if (o instanceof DTBoolean) { return ((DTBoolean) o).value.compareTo(this.value); }
        return -1;
    }
}
