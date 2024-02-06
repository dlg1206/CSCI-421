package dataTypes;

public class DTChar implements DataType {
    private String value;
    private int length;

    public DTChar(byte[] byteValue, int length) {
        if (byteValue == null)
            return;

        this.value = new String(byteValue);
        this.length = length;
    }

    public DTChar(String strValue, int length) {
        this.value = strValue;
        this.length = length;
    }

    public String getValue() {
        return value;
    }

    public int getLength() {
        return length;
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
        System.out.println("Char: " + value + ", Length: " + length);
    }
}

