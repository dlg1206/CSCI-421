package dataTypes;

import java.nio.ByteBuffer;

public class DTInteger implements DataType {
    private Integer value;

    public DTInteger(byte[] byteValue) {
        if (byteValue == null)
            return;

        this.value = ByteBuffer.wrap(byteValue).getInt();
    }

    public DTInteger(String strValue) {
        if (strValue == null)
            return;

        try {
            this.value = java.lang.Integer.parseInt(strValue);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
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
}
