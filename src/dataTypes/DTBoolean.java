package dataTypes;

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
        System.out.println("Boolean: " + value);
    }

    @Override
    public String stringValue() {
        return value ? "T" : "F";
    }
  
    public int compareTo(DataType o) {
        if (o.isNull() && this.isNull()) { return 0; } // both null
        else if (o.isNull() || this.isNull()) { return -1; } // one null
        else if (o instanceof DTBoolean) { return ((DTBoolean) o).value.compareTo(this.value); }
        return -1;
    }
}
