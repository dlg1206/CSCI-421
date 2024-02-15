package dataTypes;

public class DTBoolean implements DataType {
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
}
