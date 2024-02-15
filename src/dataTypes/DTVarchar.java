package dataTypes;

public class DTVarchar implements DataType {
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
        System.out.println("Varchar: " + value);
    }

    @Override
    public String stringValue() {
        return value;
    }
}
