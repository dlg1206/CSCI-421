package dataTypes;

import java.nio.ByteBuffer;

public class DTDouble implements DataType {
    private Double value;

    public DTDouble(byte[] byteValue) {
        if (byteValue == null)
            return;

        this.value = ByteBuffer.wrap(byteValue).getDouble();
    }

    public DTDouble(String strValue) {
        if (strValue == null)
            return;

        try {
            this.value = java.lang.Double.parseDouble(strValue);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
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
        System.out.println("Double: " + value);
    }
}

