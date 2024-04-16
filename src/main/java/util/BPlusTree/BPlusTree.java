package util.BPlusTree;

import java.util.ArrayList;
import java.util.stream.Collectors;
import dataTypes.DataType;


public class BPlusTree {
    private final int pairSize;
    private final int N;
    private Node root;

    public BPlusTree(int pageSize, int keySize, int pointerSize) {
        root = new LeafNode();
        this.pairSize = keySize + pointerSize;
        this.N = pageSize / pairSize - 1;
    }

    abstract class Node {
        ArrayList<DataType> keys;
        InternalNode parent;
        boolean isLeaf;

        Node() {
            this.keys = new ArrayList<>(N);
            this.parent = null;
        }
    }

    class InternalNode extends Node {
        ArrayList<Node> children;

        InternalNode() {
            super();
            this.isLeaf = false;
            this.children = new ArrayList<>(N + 1);
        }
    }

    class LeafNode extends Node {
        ArrayList<RecordPointer> pointers;

        LeafNode() {
            super();
            this.isLeaf = true;
            this.pointers = new ArrayList<>(N);
        }
    }

    public void insert(DataType key, RecordPointer pointer) {
        LeafNode leaf = findLeafNode(root, key);
        insertInLeafNode(leaf, key, pointer);
        if (leaf.keys.size() > N) {
            splitLeafNode(leaf);
        }
    }

    private LeafNode findLeafNode(Node node, DataType key) {
        if (node.isLeaf) {
            return (LeafNode) node;
        } else {
            InternalNode internal = (InternalNode) node;
            for (int i = 0; i < internal.keys.size(); i++) {
                if (key.compareTo(internal.keys.get(i)) < 0) {
                    return findLeafNode(internal.children.get(i), key);
                }
            }
            return findLeafNode(internal.children.get(internal.children.size() - 1), key);
        }
    }

    private void insertInLeafNode(LeafNode leaf, DataType key, RecordPointer pointer) {
        int i = 0;
        while (i < leaf.keys.size() && key.compareTo(leaf.keys.get(i)) > 0) {
            i++;
        }
        leaf.keys.add(i, key);
        leaf.pointers.add(i, pointer);
    }

    private void splitLeafNode(LeafNode leaf) {
        int splitIndex = leaf.keys.size() / 2;
        LeafNode newLeaf = new LeafNode();
        newLeaf.keys.addAll(leaf.keys.subList(splitIndex, leaf.keys.size()));
        newLeaf.pointers.addAll(leaf.pointers.subList(splitIndex, leaf.pointers.size()));

        leaf.keys.subList(splitIndex, leaf.keys.size()).clear();
        leaf.pointers.subList(splitIndex, leaf.pointers.size()).clear();

        if (leaf == root) {
            InternalNode newRoot = new InternalNode();
            newRoot.keys.add(newLeaf.keys.get(0));
            newRoot.children.add(leaf);
            newRoot.children.add(newLeaf);
            leaf.parent = newRoot;
            newLeaf.parent = newRoot;
            root = newRoot;
        } else {
            insertInParent(leaf, newLeaf.keys.get(0), newLeaf);
        }
    }

    private void insertInParent(Node oldNode, DataType key, Node newNode) {
        InternalNode parent = oldNode.parent;
        int index = parent.keys.size();
        for (int i = 0; i < parent.keys.size(); i++) {
            if (key.compareTo(parent.keys.get(i)) < 0) {
                index = i;
                break;
            }
        }

        parent.keys.add(index, key);
        parent.children.add(index + 1, newNode);
        newNode.parent = parent;

        if (parent.keys.size() > N) {
            splitInternalNode(parent);
        }
    }

    private void splitInternalNode(InternalNode node) {
        int splitIndex = node.keys.size() / 2;
        DataType upKey = node.keys.get(splitIndex);

        InternalNode newInternal = new InternalNode();
        newInternal.keys.addAll(node.keys.subList(splitIndex + 1, node.keys.size()));
        newInternal.children.addAll(node.children.subList(splitIndex + 1, node.children.size()));

        node.keys.subList(splitIndex, node.keys.size()).clear();
        node.children.subList(splitIndex + 1, node.children.size()).clear();

        for (Node child : newInternal.children) {
            child.parent = newInternal;
        }

        if (node == root) {
            InternalNode newRoot = new InternalNode();
            newRoot.keys.add(upKey);
            newRoot.children.add(node);
            newRoot.children.add(newInternal);
            node.parent = newRoot;
            newInternal.parent = newRoot;
            root = newRoot;
        } else {
            insertInParent(node, upKey, newInternal);
        }
    }

    public RecordPointer search(DataType key) {
        LeafNode leaf = findLeafNode(root, key);
        for (int i = 0; i < leaf.keys.size(); i++) {
            if (leaf.keys.get(i).compareTo(key) == 0) {
                return leaf.pointers.get(i);
            }
        }
        return null;
    }

    private void printTree(Node node, String indent, boolean last) {
        if (indent.isEmpty()) {
            System.out.println("Root: " + node.keys.stream().map(DataType::stringValue).collect(Collectors.toList()));
        } else {
            String connector = last ? "└── " : "├── ";
            System.out.println(indent + connector + (node.isLeaf ? "Leaf: " : "Internal: ") + node.keys.stream().map(DataType::stringValue).collect(Collectors.toList()));
        }
    
        if (!node.isLeaf) {
            InternalNode internal = (InternalNode) node;
            for (int i = 0; i < internal.children.size(); i++) {
                String nextIndent = indent + (last ? "    " : "│   ");
                printTree(internal.children.get(i), nextIndent, i == internal.children.size() - 1);
            }
        }
    }
    
    public void print() {
        printTree(root, "", true);
    }

    public void delete(DataType key) {
        LeafNode leaf = findLeafNode(root, key);
        if (leaf != null) {
            int index = leaf.keys.indexOf(key);
            if (index != -1) {
                // Remove the key and pointer from the leaf
                leaf.keys.remove(index);
                leaf.pointers.remove(index);
    
                if (leaf.keys.size() < N / 2) {
                    // Handle underflow
                    handleUnderflow(leaf);
                }
            }
        }
    }
    
    private void handleUnderflow(Node node) {
        if (node.isLeaf) {
            LeafNode leaf = (LeafNode) node;
            handleLeafUnderflow(leaf);
        } else {
            InternalNode internal = (InternalNode) node;
            handleInternalUnderflow(internal);
        }
    }
    
    private void handleLeafUnderflow(LeafNode leaf) {
        InternalNode parent = leaf.parent;
        if (parent == null) {
            if (leaf.keys.isEmpty()) {
                root = null;
            }
            return;
        }
    
        int leafIndex = parent.children.indexOf(leaf);
    
        // Try to merge left
        if (leafIndex > 0) {
            LeafNode leftSibling = (LeafNode) parent.children.get(leafIndex - 1);
            if (leftSibling.keys.size() + leaf.keys.size() <= N) {
                leftSibling.keys.addAll(leaf.keys);
                leftSibling.pointers.addAll(leaf.pointers);
                parent.keys.remove(leafIndex - 1);
                parent.children.remove(leafIndex);
                if (parent.keys.isEmpty()) {
                    handleUnderflow(parent);
                }
                return;
            }
        }
    
        // Try to merge right
        if (leafIndex < parent.children.size() - 1) {
            LeafNode rightSibling = (LeafNode) parent.children.get(leafIndex + 1);
            if (leaf.keys.size() + rightSibling.keys.size() <= N) {
                leaf.keys.addAll(rightSibling.keys);
                leaf.pointers.addAll(rightSibling.pointers);
                parent.keys.remove(leafIndex);
                parent.children.remove(leafIndex + 1);
                if (parent.keys.isEmpty()) {
                    handleUnderflow(parent);
                }
                return;
            }
        }
    
        // Try to borrow from the left sibling
        if (leafIndex > 0) {
            LeafNode leftSibling = (LeafNode) parent.children.get(leafIndex - 1);
            if (leftSibling.keys.size() > N / 2) {
                DataType borrowedKey = leftSibling.keys.remove(leftSibling.keys.size() - 1);
                RecordPointer borrowedPointer = leftSibling.pointers.remove(leftSibling.pointers.size() - 1);
                leaf.keys.add(0, borrowedKey);
                leaf.pointers.add(0, borrowedPointer);
                parent.keys.set(leafIndex - 1, leaf.keys.get(0));
                return;
            }
        }
    
        // Try to borrow from the right sibling
        if (leafIndex < parent.children.size() - 1) {
            LeafNode rightSibling = (LeafNode) parent.children.get(leafIndex + 1);
            if (rightSibling.keys.size() > N / 2) {
                DataType borrowedKey = rightSibling.keys.remove(0);
                RecordPointer borrowedPointer = rightSibling.pointers.remove(0);
                leaf.keys.add(borrowedKey);
                leaf.pointers.add(borrowedPointer);
                parent.keys.set(leafIndex, rightSibling.keys.get(0));
            }
        }
    }
    
    private void handleInternalUnderflow(InternalNode internal) {
        InternalNode parent = internal.parent;
        if (parent == null) {
            // Check if the internal node is now empty and needs to be removed
            if (internal.keys.size() == 0 && internal.children.size() == 1) {
                root = internal.children.get(0);
                root.parent = null;
            }
            return;
        }
    
        int index = parent.children.indexOf(internal);
    
        // Try to merge left
        if (index > 0) {
            InternalNode leftSibling = (InternalNode) parent.children.get(index - 1);
            if (leftSibling.keys.size() + internal.keys.size() < N) {
                // Transfer keys and children from internal to leftSibling
                leftSibling.keys.add(parent.keys.get(index - 1));
                leftSibling.keys.addAll(internal.keys);
                leftSibling.children.addAll(internal.children);
                internal.children.forEach(child -> child.parent = leftSibling);
    
                // Remove the reference from the parent
                parent.keys.remove(index - 1);
                parent.children.remove(internal);
    
                if (parent.keys.size() < N / 2) {
                    handleInternalUnderflow(parent);
                }
                return;
            }
        }
    
        // Try to merge right
        if (index < parent.children.size() - 1) {
            InternalNode rightSibling = (InternalNode) parent.children.get(index + 1);
            if (internal.keys.size() + rightSibling.keys.size() < N) {
                // Transfer keys and children from rightSibling to internal
                internal.keys.add(parent.keys.get(index));
                internal.keys.addAll(rightSibling.keys);
                internal.children.addAll(rightSibling.children);
                rightSibling.children.forEach(child -> child.parent = internal);
    
                // Remove the reference from the parent
                parent.keys.remove(index);
                parent.children.remove(rightSibling);
    
                if (parent.keys.size() < N / 2) {
                    handleInternalUnderflow(parent);
                }
                return;
            }
        }
    
        // Try to borrow from the left sibling
        if (index > 0) {
            InternalNode leftSibling = (InternalNode) parent.children.get(index - 1);
            if (leftSibling.keys.size() > N / 2) {
                // Borrow the largest key from the left sibling
                DataType borrowedKey = leftSibling.keys.remove(leftSibling.keys.size() - 1);
                Node borrowedChild = leftSibling.children.remove(leftSibling.children.size() - 1);
                borrowedChild.parent = internal;
    
                // Insert the borrowed key and child at the beginning of the internal node
                internal.keys.add(0, parent.keys.get(index - 1));
                internal.children.add(0, borrowedChild);
                parent.keys.set(index - 1, borrowedKey);
                return;
            }
        }
    
        // Try to borrow from the right sibling
        if (index < parent.children.size() - 1) {
            InternalNode rightSibling = (InternalNode) parent.children.get(index + 1);
            if (rightSibling.keys.size() > N / 2) {
                // Borrow the smallest key from the right sibling
                DataType borrowedKey = rightSibling.keys.remove(0);
                Node borrowedChild = rightSibling.children.remove(0);
                borrowedChild.parent = internal;
    
                // Append the borrowed key and child to the end of the internal node
                internal.keys.add(parent.keys.get(index));
                internal.children.add(borrowedChild);
                parent.keys.set(index, borrowedKey);
                return;
            }
        }
    }
    
}

