package dataTypes;

public class DTChar implements DataType, Comparable<DataType> {
    private String value;
    private int maxLength;

    public DTChar(byte[] byteValue, int maxLength) {
        if (byteValue == null)
            return;
        
        this.value = new String(byteValue);
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
    public String stringValue() {
        return value;
    }

    public int compareTo(DataType o) {
        if (o instanceof DTChar) {
            return ((DTChar) o).value.compareTo(this.value);
        }
        return -1;
    }
}

