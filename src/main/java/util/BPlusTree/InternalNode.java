package util.BPlusTree;

import java.util.ArrayList;

public class InternalNode extends Node {
    public ArrayList<Integer> children; // contains page numbers of children

    public InternalNode(int N) {
        super(N);
        this.isLeaf = false;
        this.children = new ArrayList<>(N + 1);
    }
}
