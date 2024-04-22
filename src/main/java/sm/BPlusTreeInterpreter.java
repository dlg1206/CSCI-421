package sm;

import util.BPlusTree.BPlusTree;
import util.BPlusTree.RecordPointer;

public class BPlusTreeInterpreter {

    public static byte[] convertNodeToBinary(BPlusTree.Node node) {
        byte[] bdata;

        if (node.isLeaf)
            bdata = convertLeafNodeToBinary((BPlusTree.LeafNode) node);
        else
            bdata = convertInternalNodeToBinary((BPlusTree.InternalNode) node);

        return bdata;
    }

    private static byte[] convertInternalNodeToBinary(BPlusTree.InternalNode node) {
        return null;
    }

    private static byte[] convertLeafNodeToBinary(BPlusTree.LeafNode node) {
        return null;
    }

    public static BPlusTree.Node convertBinaryToNode(byte[] data) {
        BPlusTree.Node node;

        boolean isLeaf = false;
        if (isLeaf)
            node = convertBinaryToLeafNode(data);
        else
            node = convertBinaryToInternalNode(data);

        return node;
    }

    private static BPlusTree.InternalNode convertBinaryToInternalNode(byte[] data) {
        BPlusTree.InternalNode node = null;

        return node;
    }

    private static BPlusTree.LeafNode convertBinaryToLeafNode(byte[] data) {
        BPlusTree.LeafNode node = new BPlusTree();

        return node;
    }
}
