package util.BPlusTree;

import dataTypes.DataType;
import sm.Page;

import java.util.ArrayList;

public abstract class Node {
    public ArrayList<DataType> keys;
    public Integer parentNum;
    public int pageNum;
    public boolean isLeaf;
    public Page page;

    public Node(int N, int pageNum, Integer parentNum, Page p) {
        this.keys = new ArrayList<>(N);
        this.parentNum = parentNum;
        this.pageNum = pageNum;
        this.page = p;
    }
}

