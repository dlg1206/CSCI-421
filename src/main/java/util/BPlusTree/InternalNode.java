package util.BPlusTree;

import sm.Page;

import java.util.ArrayList;

public class InternalNode extends Node {
    public ArrayList<Integer> children; // contains page numbers of children

    public InternalNode(int N, int pageNum, Integer parentNum) {
        this(N, pageNum, parentNum, null);
    }

    public InternalNode(int N, int pageNum, Integer parentNum, Page p) {
        super(N, pageNum, parentNum, p);
        this.isLeaf = false;
        this.children = new ArrayList<>(N + 1);
    }
}
