package util.BPlusTree;

import sm.Page;

import java.util.ArrayList;

public class LeafNode extends Node {
    public ArrayList<RecordPointer> pointers;

    public LeafNode(int N, int pageNum, Integer parentNum) {
        this(N, pageNum, parentNum, null);
    }

    public LeafNode(int N, int pageNum, Integer parentNum, Page p) {
        super(N, pageNum, parentNum, p);
        this.isLeaf = true;
        this.pointers = new ArrayList<>(N);
    }
}
