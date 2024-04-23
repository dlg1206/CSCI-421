package util.BPlusTree;

import java.util.ArrayList;

public class LeafNode extends Node {
    public ArrayList<RecordPointer> pointers;

    public LeafNode(int N) {
        super(N);
        this.isLeaf = true;
        this.pointers = new ArrayList<>(N);
    }
}
