package dataTypes;

public class DTVarchar implements DataType {
    private String value;
    private int length;

    public DTVarchar(byte[] byteValue, int length) {
        if (byteValue == null)
            return;

        this.value = new String(byteValue);
        this.length = length;
    }

    public DTVarchar(String strValue, int length) {
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
        System.out.println("Varchar: " + value + ", Length: " + length);
    }
}
