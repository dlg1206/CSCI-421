package sm;

import catalog.Attribute;
import dataTypes.DTInteger;
import dataTypes.DataType;
import util.BPlusTree.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * <b>DBFile:</b> IndexFile.java
 * <p>
 * <b>Description:</b> Utility file to store metadata about the index of a database file
 *
 * @author Derek Garcia
 */
public class IndexFile extends DBFile{

    private final static int POINTER_SIZE = 8; // Size of 2 integers (Page Num, Page Index)
    private final int Capacity;
    private int NodeCount;
    private final Attribute PKAttr;
    private final PageBuffer Buffer;

    /**
     * Create a new Index file
     *
     * @param databaseRoot Root path of the database
     * @param tableID      table ID of the file this is an index of
     * @throws IOException Failed to create or open file
     */
    public IndexFile(String databaseRoot, int tableID, PageBuffer buffer, Attribute pkAttr, int pageSize) throws IOException {
        super(databaseRoot, tableID, INDEX_FILE_EXTENSION);
        PKAttr = pkAttr;
        int pairSize = PKAttr.getMaxDataLength() + POINTER_SIZE;
        this.Capacity = (pageSize / pairSize) - 1;
        this.Buffer = buffer;
        File f = toFile();
        NodeCount = readNodeCount();
        if (f.length() == 1) {
            LeafNode root = new LeafNode(Capacity, 0, null);
            updateRootNode(root);
            buffer.flush();
        }
    }

    private Node getRootNode() throws IOException {
        try (RandomAccessFile raf = toRandomAccessFile()) {
            // first 4 bytes of index file is reserved for number of pages,
            // next 4 bytes contains the root node page number
            byte[] buffer = new byte[4];
            raf.read(buffer, 4, 4);
            int rootPageNum = new DTInteger(Arrays.copyOfRange(buffer, 0, 4)).getValue();
            return getNodeFromBuffer(rootPageNum);
        }
    }

    private void updateRootNode(Node newRoot) throws IOException {
        try (RandomAccessFile raf = toRandomAccessFile()) {
            byte[] rootPageNum = new DTInteger(Integer.toString(newRoot.pageNum)).convertToBytes();
            raf.seek(4);
            raf.write(rootPageNum, 0, 4);
            writeNode(newRoot);
        }
    }

    private int readNodeCount() throws IOException {
        try (RandomAccessFile raf = toRandomAccessFile()) {
            byte[] buffer = new byte[4];
            raf.read(buffer, 0, 4);
            return ByteBuffer.wrap(buffer).getInt();
        }
    }

    private int nextNodeValue() throws IOException {
        try (RandomAccessFile raf = toRandomAccessFile()) {
            // update page count
            NodeCount = readNodeCount() + 1;
            raf.write(ByteBuffer.allocate(4).putInt(NodeCount).array(), 0, 4);
            return NodeCount;
        }
    }

    public void insertPointer(DataType key, RecordPointer recordPointer) throws IOException {
        LeafNode leaf = findLeafNode(getRootNode(), key);
        insertInLeafNode(leaf, key, recordPointer);
    }

    public void deletePointer(DataType primaryKey){
        // TODO b+ delete pointer
    }

    private LeafNode findLeafNode(Node node, DataType key) throws IOException {
        if (node.isLeaf) {
            return (LeafNode) node;
        } else {
            InternalNode internal = (InternalNode) node;
            for (int i = 0; i < internal.keys.size(); i++) {
                if (key.compareTo(internal.keys.get(i)) > 0) {
                    return findLeafNode(getNodeFromBuffer(internal.children.get(i)), key);
                }
            }
            return findLeafNode(getNodeFromBuffer(internal.children.getLast()), key);
        }
    }

    private void insertInLeafNode(LeafNode leaf, DataType key, RecordPointer pointer) throws IOException {
        int i = 0;
        while (i < leaf.keys.size() && key.compareTo(leaf.keys.get(i)) < 0) {
            i++;
        }
        leaf.keys.add(i, key);
        leaf.pointers.add(i, pointer);

        if (leaf.keys.size() > Capacity) {
            splitLeafNode(leaf);
        }

        writeNode(leaf);
    }

    private void splitLeafNode(LeafNode leaf) throws IOException {
        int splitIndex = leaf.keys.size() / 2;
        LeafNode newLeaf = new LeafNode(Capacity, nextNodeValue(), leaf.parentNum);
        newLeaf.keys.addAll(leaf.keys.subList(splitIndex, leaf.keys.size()));
        newLeaf.pointers.addAll(leaf.pointers.subList(splitIndex, leaf.pointers.size()));

        leaf.keys.subList(splitIndex, leaf.keys.size()).clear();
        leaf.pointers.subList(splitIndex, leaf.pointers.size()).clear();

        if (leaf.parentNum == null) {
            InternalNode newRoot = new InternalNode(Capacity, nextNodeValue(), null);
            newRoot.keys.add(newLeaf.keys.getFirst());
            newRoot.children.add(leaf.pageNum);
            newRoot.children.add(newLeaf.pageNum);
            leaf.parentNum = newRoot.pageNum;
            newLeaf.parentNum = newRoot.pageNum;
            updateRootNode(newRoot);
            writeNode(leaf);
        } else {
            insertInParent(leaf, newLeaf.keys.getFirst(), newLeaf);
        }
        writeNode(newLeaf);
    }

    private void insertInParent(Node oldNode, DataType key, Node newNode) throws IOException {
        InternalNode parent = (InternalNode) getNodeFromBuffer(oldNode.parentNum);
        int index = parent.keys.size();
        for (int i = 0; i < parent.keys.size(); i++) {
            if (key.compareTo(parent.keys.get(i)) > 0) {
                index = i;
                break;
            }
        }

        parent.keys.add(index, key);
        parent.children.add(index + 1, newNode.pageNum);
        newNode.parentNum = parent.pageNum;

        if (parent.keys.size() > Capacity) {
            splitInternalNode(parent);
        }
        writeNode(parent);
    }

    private void splitInternalNode(InternalNode node) throws IOException {
        int splitIndex = node.keys.size() / 2;
        DataType upKey = node.keys.get(splitIndex);

        InternalNode newInternal = new InternalNode(Capacity, nextNodeValue(), node.parentNum);
        newInternal.keys.addAll(node.keys.subList(splitIndex + 1, node.keys.size()));
        newInternal.children.addAll(node.children.subList(splitIndex + 1, node.children.size()));

        node.keys.subList(splitIndex, node.keys.size()).clear();
        node.children.subList(splitIndex + 1, node.children.size()).clear();

        for (int childNum : newInternal.children) {
            Node child = getNodeFromBuffer(childNum);
            child.parentNum = newInternal.pageNum;
        }

        if (node.parentNum == null) {
            InternalNode newRoot = new InternalNode(Capacity, nextNodeValue(), null);
            newRoot.keys.add(upKey);
            newRoot.children.add(node.pageNum);
            newRoot.children.add(newInternal.pageNum);
            node.parentNum = newRoot.pageNum;
            newInternal.parentNum = newRoot.pageNum;
            updateRootNode(newRoot);
            writeNode(node);
        } else {
            insertInParent(node, upKey, newInternal);
        }
        writeNode(newInternal);
    }

    public RecordPointer search(DataType key) throws IOException {
        LeafNode leaf = findLeafNode(getRootNode(), key);
        for (int i = 0; i < leaf.keys.size(); i++) {
            if (leaf.keys.get(i).compareTo(key) == 0) {
                return leaf.pointers.get(i);
            }
        }
        return null;
    }

    @Override
    public int getTableID() {
        return fileID;
    }

    @Override
    public TableFile getSwapFile() throws IOException {
        return null;
    }

    private Node getNodeFromBuffer(int pageNum) throws IOException {
        Page p = Buffer.readFromBuffer(fileID, pageNum, false, this);
        return BPlusTreeInterpreter.convertBinaryToNode(p, pageNum, PKAttr, Capacity);
    }

    private void writeNode(Node n) throws IOException {
        byte[] nodeData = BPlusTreeInterpreter.convertNodeToBinary(n);
        if (n.page == null) {
            n.page = new Page(this, nodeData.length, n.pageNum, nodeData, true);
            Buffer.writeToBuffer(n.page);
        }
        n.page.setData(nodeData);
    }

    public void print() throws IOException {
        printTree(getRootNode(), "", true);
    }

    private void printTree(Node node, String indent, boolean last) throws IOException {
        if (indent.isEmpty()) {
            System.out.println("Root: " + node.keys.stream().map(DataType::stringValue).toList());
        } else {
            String connector = last ? "└── " : "├── ";
            System.out.println(indent + connector + (node.isLeaf ? "Leaf: " : "Internal: ") + node.keys.stream().map(DataType::stringValue).toList());
        }

        if (!node.isLeaf) {
            InternalNode internal = (InternalNode) node;
            for (int i = 0; i < internal.children.size(); i++) {
                String nextIndent = indent + (last ? "    " : "│   ");
                printTree(getNodeFromBuffer(internal.children.get(i)), nextIndent, i == internal.children.size() - 1);
            }
        }
    }
}
