package dataTypes;

public interface DataType extends Comparable<DataType> {
    byte[] convertToBytes();
    boolean isNull();
    void printValue(); // for debug purposes
    String stringValue();
}



