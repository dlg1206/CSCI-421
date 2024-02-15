package dataTypes;

public interface DataType {
    byte[] convertToBytes();
    boolean isNull();
    void printValue(); // for debug purposes
}



