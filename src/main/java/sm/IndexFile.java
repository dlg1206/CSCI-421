package sm;

import catalog.Attribute;
import dataTypes.DataType;
import util.BPlusTree.*;

import java.io.*;
import java.nio.ByteBuffer;

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
    private final int PageSize;
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
        this.PageSize = pageSize;
        this.Capacity = (pageSize / pairSize) - 1;
        this.Buffer = buffer;
        NodeCount = readNodeCount();
        if (NodeCount == 0) {
            LeafNode root = new LeafNode(Capacity, 0, null);
            updateRootNode(root);
            buffer.flush();
        }
    }

    private Node getRootNode() throws IOException {
        try (RandomAccessFile raf = toRandomAccessFile()) {
            // first 4 bytes of index file is reserved for number of pages,
            // next 4 bytes contains the root node page number
            byte[] buffer = new byte[Integer.BYTES];
            raf.seek(Integer.BYTES);
            raf.read(buffer, 0, Integer.BYTES);
            int rootPageNum = ByteBuffer.wrap(buffer).getInt();
            return getNodeFromBuffer(rootPageNum);
        }
    }

    private void updateRootNode(Node newRoot) throws IOException {
        try (RandomAccessFile raf = toRandomAccessFile()) {
            raf.seek(Integer.BYTES);    // Skip the page count int
            raf.write(ByteBuffer.allocate(Integer.BYTES).putInt(newRoot.pageNum).array(), 0, Integer.BYTES);
            writeNode(newRoot);
//            Buffer.flush();
        }
    }

    private int readNodeCount() throws IOException {
        try (RandomAccessFile raf = toRandomAccessFile()) {
            byte[] buffer = new byte[Integer.BYTES];
            raf.read(buffer, 0, Integer.BYTES);
            return ByteBuffer.wrap(buffer).getInt();
        }
    }

    private int nextNodeValue() throws IOException {
        try (RandomAccessFile raf = toRandomAccessFile()) {
            // update page count
            NodeCount = readNodeCount() + 1;
            raf.write(ByteBuffer.allocate(Integer.BYTES).putInt(NodeCount).array(), 0, Integer.BYTES);
            return NodeCount;
        }
    }

    public void insertPointer(DataType key, RecordPointer recordPointer) throws IOException {
        LeafNode leaf = findLeafNode(getRootNode(), key);
        insertInLeafNode(leaf, key, recordPointer);
    }


    public void updatePointer(DataType key, RecordPointer recordPointer) throws IOException {
        LeafNode leaf = findLeafNode(getRootNode(), key);
        int i = 0;
        while (i < leaf.keys.size() && key.compareTo(leaf.keys.get(i)) < 0) {
            i++;
        }
        leaf.pointers.remove(i);
        leaf.pointers.add(i, recordPointer);
        writeNode(leaf);
    }

    public void deletePointer(DataType primaryKey) throws IOException {
        LeafNode leaf = findLeafNode(getRootNode(), primaryKey);
        int index = leaf.keys.indexOf(primaryKey);
        if (index != -1) {
            // Remove the key and pointer from the leaf
            leaf.keys.remove(index);
            leaf.pointers.remove(index);

            if (leaf.keys.size() < Capacity / 2) {
                // Handle underflow
                handleUnderflow(leaf);
            }
            writeNode(leaf);
        }
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
            writeNode(leaf);
            updateRootNode(newRoot);
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

    private void handleUnderflow(Node node) throws IOException {
        if (node.isLeaf) {
            LeafNode leaf = (LeafNode) node;
            handleLeafUnderflow(leaf);
        } else {
            InternalNode internal = (InternalNode) node;
            handleInternalUnderflow(internal);
        }
    }

    private void handleLeafUnderflow(LeafNode leaf) throws IOException {
        if (leaf.parentNum == null) {
            if (leaf.keys.isEmpty()) {
                leaf.pointers.clear();
            }
            return;
        }

        InternalNode parent = (InternalNode) getNodeFromBuffer(leaf.parentNum);

        int leafIndex = parent.children.indexOf(leaf.pageNum);

        // Try to merge left
        if (leafIndex > 0) {
            LeafNode leftSibling = (LeafNode) getNodeFromBuffer(parent.children.get(leafIndex - 1));
            if (leftSibling.keys.size() + leaf.keys.size() <= Capacity) {
                leftSibling.keys.addAll(leaf.keys);
                leftSibling.pointers.addAll(leaf.pointers);
                parent.keys.remove(leafIndex - 1);
                parent.children.remove(leafIndex);
                if (parent.keys.isEmpty()) {
                    handleUnderflow(parent);
                }
                writeNode(leftSibling);
                writeNode(parent);
                return;
            }
        }

        // Try to merge right
        if (leafIndex < parent.children.size() - 1) {
            LeafNode rightSibling = (LeafNode) getNodeFromBuffer(parent.children.get(leafIndex + 1));
            if (leaf.keys.size() + rightSibling.keys.size() <= Capacity) {
                leaf.keys.addAll(rightSibling.keys);
                leaf.pointers.addAll(rightSibling.pointers);
                parent.keys.remove(leafIndex);
                parent.children.remove(leafIndex + 1);
                if (parent.keys.isEmpty()) {
                    handleUnderflow(parent);
                }
                writeNode(leaf);
                writeNode(parent);
                return;
            }
        }

        // Try to borrow from the left sibling
        if (leafIndex > 0) {
            LeafNode leftSibling = (LeafNode) getNodeFromBuffer(parent.children.get(leafIndex - 1));
            if (leftSibling.keys.size() > Capacity / 2) {
                DataType borrowedKey = leftSibling.keys.removeLast();
                RecordPointer borrowedPointer = leftSibling.pointers.removeLast();
                leaf.keys.addFirst(borrowedKey);
                leaf.pointers.addFirst(borrowedPointer);
                parent.keys.set(leafIndex - 1, leaf.keys.getFirst());
                writeNode(leftSibling);
                writeNode(parent);
                writeNode(leaf);
                return;
            }
        }

        // Try to borrow from the right sibling
        if (leafIndex < parent.children.size() - 1) {
            LeafNode rightSibling = (LeafNode) getNodeFromBuffer(parent.children.get(leafIndex + 1));
            if (rightSibling.keys.size() > Capacity / 2) {
                DataType borrowedKey = rightSibling.keys.removeFirst();
                RecordPointer borrowedPointer = rightSibling.pointers.removeFirst();
                leaf.keys.add(borrowedKey);
                leaf.pointers.add(borrowedPointer);
                parent.keys.set(leafIndex, rightSibling.keys.getFirst());
                writeNode(rightSibling);
                writeNode(parent);
                writeNode(leaf);
            }
        }
    }

    private void handleInternalUnderflow(InternalNode internal) throws IOException {
        if (internal.parentNum == null) {
            // Check if the internal node is now empty and needs to be removed
            if (internal.keys.isEmpty() && internal.children.size() == 1) {
                Node newRoot = getNodeFromBuffer(internal.children.getFirst());
                newRoot.parentNum = null;
                updateRootNode(newRoot);
            }
            return;
        }

        InternalNode parent = (InternalNode) getNodeFromBuffer(internal.parentNum);

        int index = parent.children.indexOf(internal.pageNum);

        // Try to merge left
        if (index > 0) {
            InternalNode leftSibling = (InternalNode) getNodeFromBuffer(parent.children.get(index - 1));
            if (leftSibling.keys.size() + internal.keys.size() < Capacity) {
                // Transfer keys and children from internal to leftSibling
                leftSibling.keys.add(parent.keys.get(index - 1));
                leftSibling.keys.addAll(internal.keys);
                leftSibling.children.addAll(internal.children);
                for (int i = 0; i < internal.children.size(); i++) {
                    Node child = getNodeFromBuffer(internal.children.get(i));
                    child.parentNum = leftSibling.pageNum;
                    writeNode(child);
                }

                // Remove the reference from the parent
                parent.keys.remove(index - 1);
                parent.children.remove(internal.pageNum);

                if (parent.keys.size() < Capacity / 2) {
                    handleInternalUnderflow(parent);
                }
                writeNode(parent);
                writeNode(leftSibling);
                return;
            }
        }

        // Try to merge right
        if (index < parent.children.size() - 1) {
            InternalNode rightSibling = (InternalNode) getNodeFromBuffer(parent.children.get(index + 1));
            if (internal.keys.size() + rightSibling.keys.size() < Capacity) {
                // Transfer keys and children from rightSibling to internal
                internal.keys.add(parent.keys.get(index));
                internal.keys.addAll(rightSibling.keys);
                internal.children.addAll(rightSibling.children);
                for (int i = 0; i < internal.children.size(); i++) {
                    Node child = getNodeFromBuffer(internal.children.get(i));
                    child.parentNum = rightSibling.pageNum;
                    writeNode(child);
                }

                // Remove the reference from the parent
                parent.keys.remove(index);
                parent.children.remove(rightSibling.pageNum);
                writeNode(internal);
                writeNode(parent);

                if (parent.keys.size() < Capacity / 2) {
                    handleInternalUnderflow(parent);
                }
                return;
            }
        }

        // Try to borrow from the left sibling
        if (index > 0) {
            InternalNode leftSibling = (InternalNode) getNodeFromBuffer(parent.children.get(index - 1));
            if (leftSibling.keys.size() > Capacity / 2) {
                // Borrow the largest key from the left sibling
                DataType borrowedKey = leftSibling.keys.removeLast();
                Node borrowedChild = getNodeFromBuffer(leftSibling.children.removeLast());
                borrowedChild.parentNum = internal.pageNum;

                // Insert the borrowed key and child at the beginning of the internal node
                internal.keys.addFirst(parent.keys.get(index - 1));
                internal.children.addFirst(borrowedChild.pageNum);
                parent.keys.set(index - 1, borrowedKey);
                writeNode(borrowedChild);
                writeNode(leftSibling);
                writeNode(parent);
                writeNode(internal);
                return;
            }
        }

        // Try to borrow from the right sibling
        if (index < parent.children.size() - 1) {
            InternalNode rightSibling = (InternalNode) getNodeFromBuffer(parent.children.get(index + 1));
            if (rightSibling.keys.size() > Capacity / 2) {
                // Borrow the smallest key from the right sibling
                DataType borrowedKey = rightSibling.keys.removeFirst();
                Node borrowedChild = getNodeFromBuffer(rightSibling.children.removeFirst());
                borrowedChild.parentNum = internal.pageNum;

                // Append the borrowed key and child to the end of the internal node
                internal.keys.add(parent.keys.get(index));
                internal.children.add(borrowedChild.pageNum);
                parent.keys.set(index, borrowedKey);
                writeNode(borrowedChild);
                writeNode(rightSibling);
                writeNode(parent);
                writeNode(internal);
            }
        }
    }

    @Override
    public int getTableID() {
        return fileID;
    }

    @Override
    public TableFile getSwapFile() throws IOException {
        return null; // This better not ever run
    }

    private Node getNodeFromBuffer(int pageNum) throws IOException {
        Page p = Buffer.readFromBuffer(fileID, pageNum, false, this);
        return BPlusTreeInterpreter.convertBinaryToNode(p, pageNum, PKAttr, Capacity);
    }

    private void writeNode(Node n) throws IOException {
        byte[] nodeData = BPlusTreeInterpreter.convertNodeToBinary(n);
        if (n.page == null) {
            n.page = new Page(this, PageSize, n.pageNum, nodeData, true);
            Buffer.writeToBuffer(n.page);
//            Buffer.flush();
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
