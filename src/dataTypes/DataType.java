package dataTypes;

import java.util.Comparator;

interface DataType extends Comparable<DataType> {
    byte[] convertToBytes();
    boolean isNull();
    void printValue(); // for debug purposes
}



