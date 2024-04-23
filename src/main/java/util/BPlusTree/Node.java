package util.BPlusTree;

import dataTypes.DataType;

import java.util.ArrayList;

public abstract class Node {
    public ArrayList<DataType> keys;
    public InternalNode parent;
    public boolean isLeaf;

    public Node(int N) {
        this.keys = new ArrayList<>(N);
        this.parent = null;
    }
}

