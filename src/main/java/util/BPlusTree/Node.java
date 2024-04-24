package util.BPlusTree;

import dataTypes.DataType;

import java.util.ArrayList;

public abstract class Node {
    public ArrayList<DataType> keys;
    public Integer parentNum;
    public int pageNum;
    public boolean isLeaf;

    public Node(int N, int pageNum, Integer parentNum) {
        this.keys = new ArrayList<>(N);
        this.parentNum = parentNum;
        this.pageNum = pageNum;
    }
}

