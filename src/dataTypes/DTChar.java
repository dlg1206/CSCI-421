package dataTypes;

public class DTChar implements DataType, Comparable<DataType> {
    private String value;

    public DTChar(byte[] byteValue) {
        if (byteValue == null)
            return;

        this.value = new String(byteValue);
    }

    public DTChar(String strValue) {
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
        System.out.println("Char: " + value);
    }

    @Override
    public int compareTo(DataType o) {
        if (o instanceof DTChar) {
            return ((DTChar) o).value.compareTo(this.value);
        }
        return -1;
    }
}

