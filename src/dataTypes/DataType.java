package dataTypes;

import java.util.Comparator;

interface DataType {
    byte[] convertToBytes();
    boolean isNull();
    void printValue(); // for debug purposes
}



