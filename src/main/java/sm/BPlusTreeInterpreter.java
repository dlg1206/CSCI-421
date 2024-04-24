package sm;

import catalog.Attribute;
import dataTypes.*;
import util.BPlusTree.Node;
import util.BPlusTree.InternalNode;
import util.BPlusTree.LeafNode;
import util.BPlusTree.RecordPointer;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class BPlusTreeInterpreter {

    public static byte[] convertNodeToBinary(Node node) {
        ByteArrayOutputStream pageData = new ByteArrayOutputStream();

        // first byte is flag indicating if node is leaf node
        if (node.isLeaf)
            pageData.writeBytes(new byte[] {1});
        else
            pageData.writeBytes(new byte[] {0});

        // write parent's page number (4 bytes)
        int parentNum = node.parentNum == null ? -1 : node.parentNum;
        pageData.writeBytes(ByteBuffer.allocate(4).putInt(parentNum).array());

        // write number of pairs (4 bytes)
        pageData.writeBytes(ByteBuffer.allocate(4).putInt(node.keys.size()).array());

        // write key values
        for (DataType key: node.keys) {
            if (key instanceof DTVarchar) {
                byte[] varcharData = key.convertToBytes();

                // for varchar, must write 1 byte for the length
                pageData.write((char) varcharData.length);
                pageData.writeBytes(varcharData);
            } else {
                pageData.writeBytes(key.convertToBytes());
            }
        }

        // if leaf node, write record pointers
        if (node.isLeaf) {
            for (RecordPointer recordPointer : ((LeafNode) node).pointers) {
                pageData.writeBytes(ByteBuffer.allocate(4).putInt(recordPointer.pageNumber).array());
                pageData.writeBytes(ByteBuffer.allocate(4).putInt(recordPointer.index).array());
            }
        }
        // else if internal node, write page pointers
        else {
            for (Integer pageNumber : ((InternalNode) node).children) {
                pageData.writeBytes(ByteBuffer.allocate(4).putInt(pageNumber).array());
            }
        }

        return pageData.toByteArray();
    }


    public static Node convertBinaryToNode(byte[] data, Integer pageNum, Attribute attribute, int N) {
        Node node;

        // read flag indicating if node is leaf or internal node
        ByteBuffer numRecBuff = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 5));
        int isLeaf =  numRecBuff.get();
        Integer parentNum =  numRecBuff.getInt();
        parentNum = parentNum == -1 ? null : parentNum;
        if (isLeaf == 1) {
            node = new LeafNode(N, pageNum, parentNum);
            node.isLeaf = true;
        }
        else {
            node = new InternalNode(N, pageNum, parentNum);
            node.isLeaf = false;
        }

        int dataIdx = 5; // skip index 0 (isLeaf) and index 1-4 (parentNum)

        // read number of pairs (key-pointer pairs)
        int numPairs = ByteBuffer.wrap(Arrays.copyOfRange(data, dataIdx, dataIdx + 4)).getInt();
        dataIdx += 4;

        // read keys
        ArrayList<DataType> keys = new ArrayList<>();
        for (int i = 0; i < numPairs; i++) {
            switch (attribute.getDataType()) {
                case INTEGER:
                    // 4 bytes
                    keys.add(new DTInteger(Arrays.copyOfRange(data, dataIdx, dataIdx + 4)));
                    dataIdx += 4;
                    break;

                case DOUBLE:
                    // 8 bytes
                    keys.add(new DTDouble(Arrays.copyOfRange(data, dataIdx, dataIdx + 8)));
                    dataIdx += 8;
                    break;

                case BOOLEAN:
                    // 1 byte
                    keys.add(new DTBoolean(Arrays.copyOfRange(data, dataIdx, dataIdx + 1)));
                    dataIdx += 1;
                    break;

                case CHAR:
                    // bytes based on max length
                    int length = attribute.getMaxDataLength(); // get max length of char

                    keys.add(new DTChar(Arrays.copyOfRange(data, dataIdx, dataIdx + length), length));
                    dataIdx += length;
                    break;

                case VARCHAR:
                    // 1 byte for length + n bytes
                    // get length of varchar
                    int varcharLength = data[dataIdx];
                    dataIdx += 1;
                    keys.add(new DTVarchar(Arrays.copyOfRange(data, dataIdx, dataIdx + varcharLength)));
                    dataIdx += varcharLength;
                    break;
            }
        }
        node.keys = keys;

        // if leaf node, read record pointers
        if (isLeaf == 1) {
            ArrayList<RecordPointer> recordPointers = new ArrayList<>();

            for (int i = 0; i < numPairs; i++) {
                int pageNumber = ByteBuffer.wrap(Arrays.copyOfRange(data, dataIdx, dataIdx + 4)).getInt();
                dataIdx += 4;
                int index = ByteBuffer.wrap(Arrays.copyOfRange(data, dataIdx, dataIdx + 4)).getInt();
                dataIdx += 4;

                recordPointers.add(new RecordPointer(pageNumber, index));
            }

            ((LeafNode) node).pointers = recordPointers;
        }
        // else if internal node, read page pointers
        else {
            ArrayList<Integer> pagePointers = new ArrayList<>();

            for (int i = 0; i < numPairs + 1; i++) {
                pagePointers.add(ByteBuffer.wrap(Arrays.copyOfRange(data, dataIdx, dataIdx + 4)).getInt());
                dataIdx += 4;
            }

            ((InternalNode) node).children = pagePointers;
        }

        return node;
    }
}
