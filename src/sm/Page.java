package sm;



import catalog.Attribute;
import catalog.NotSupportedConstraint;
import dataTypes.*;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <b>File:</b> Page.java
 * <p>
 * <b>Description:</b> Utility Page object to hold metadata about page
 *
 * @author Derek Garcia, Ryan Nowak
 */
class Page {
    /*
    Table page structure:
        <numRecords>
        <row1>
        <row2>
        ....
        <rowN>
     */
    public int tableID;
    public int number;
    private List<Attribute> attributes;

    // public byte[] data;
    public List<Record> records;


    /**
     * Create new Page
     *
     * @param tableID Table ID of the page
     * @param number  Page Number
     * @param data    Raw binary data of the page
     */
    public Page(int tableID, int number, List<Attribute> attributes, byte[] data) {
        this.tableID = tableID;
        this.number = number;
        this.attributes = attributes;
        this.records = convertToRecords(data);
    }

    // used whe reading a page from hardware
    private List<Record> convertToRecords(byte[] data) {
        List<Record> records = new ArrayList<>();
        int numRecords = data[0];
        int dataIdx = 1; // skip index 0 which contains number of records

        // parse each record
        for (int i = 0; i < numRecords; i++) {
            List<DataType> dataTypes = new ArrayList<>();

            // get null bitmap
            // if there are more than 8 attributes in a table, then bitmap will take up more than 1 byte
            int bitmapSize = ((attributes.size()-1) / 8) + 1;
            byte[] bitmap = Arrays.copyOfRange(data, dataIdx, dataIdx + bitmapSize);
            dataIdx += bitmapSize;

            for (int j = 0; j < attributes.size(); j++) {
                // check if attribute is null
                if (getBit(bitmap, j) == 1) {

                }

                switch (attributes.get(j).getDataType()) {
                    case INTEGER:
                        // 4 bytes

                        // check if attribute is null
                        if (getBit(bitmap, j) == 1) {
                            dataTypes.add(new DTInteger((byte[]) null));
                        }
                        else {
                            dataTypes.add(new DTInteger(Arrays.copyOfRange(data, dataIdx, dataIdx + 4)));
                            dataIdx += 4; // change to constants for neater code?
                        }
                        break;

                    case DOUBLE:
                        // 8 bytes

                        // check if attribute is null
                        if (getBit(bitmap, j) == 1) {
                            dataTypes.add(new DTDouble((byte[]) null));
                        }
                        else {
                            dataTypes.add(new DTDouble(Arrays.copyOfRange(data, dataIdx, dataIdx + 8)));
                            dataIdx += 8;
                        }
                        break;

                    case BOOLEAN:
                        // 1 byte

                        // check if attribute is null
                        if (getBit(bitmap, j) == 1) {
                            dataTypes.add(new DTBoolean((byte[]) null));
                        }
                        else {
                            dataTypes.add(new DTBoolean(Arrays.copyOfRange(data, dataIdx, dataIdx + 1)));
                            dataIdx += 1;
                        }
                        break;

                    case CHAR:
                        // bytes based on max length

                        // check if attribute is null
                        if (getBit(bitmap, j) == 1) {
                            dataTypes.add(new DTChar((byte[]) null, 0));
                        }
                        else {
                            try {
                                int length = attributes.get(j).getMaxDataLength(); // get max length of char

                                dataTypes.add(new DTChar(Arrays.copyOfRange(data, dataIdx, dataIdx + length), length));
                                dataIdx += length;
                            } catch (NotSupportedConstraint e) {
                                e.printStackTrace();
                            }
                        }
                        break;

                    case VARCHAR:
                        // 1 byte for length + n bytes

                        // check if attribute is null
                        if (getBit(bitmap, j) == 1) {
                            dataTypes.add(new DTVarchar((byte[]) null));
                        }
                        else {
                            // get length of varchar
                            int length = data[dataIdx];
                            dataIdx += 1;
                            dataTypes.add(new DTVarchar(Arrays.copyOfRange(data, dataIdx, dataIdx + length)));
                            dataIdx += length;
                        }
                        break;
                }
            }

            records.add(new Record(dataTypes));
        }

        return records;
    }

    private static int getBit(byte[] ba, int pos) {
        int bytePos = pos/8;

        if (bytePos >= ba.length)
            throw new RuntimeException("bit position out of bounds");

        int bitPos = 7 - pos % 8;
        byte b = (byte)ba[bytePos];

        return (b >> bitPos) & 1;
    }

    public static void setBit(byte[] ba, int pos, int val) {
        int posByte = pos/8;
        int posBit = pos%8;
        byte oldByte = ba[posByte];
        oldByte = (byte) (((0xFF7F>>posBit) & oldByte) & 0x00FF);
        byte newByte = (byte) ((val<<(8-(posBit+1))) | oldByte);
        ba[posByte] = newByte;
    }

    /**
     * Get all records from page
     *
     * @return
     */
    public List<Record> getAllRecords () {
        return records;
    }

    // used when writing a page to hardware
    public byte[] convertToBytes () {
        ByteArrayOutputStream pageData = new ByteArrayOutputStream();

        // write number of records (1 byte)
        pageData.write((char) records.size());

        int bitmapSize = ((attributes.size()-1) / 8) + 1;

        // write each record
        for (Record record: records) {
            byte[] bitmap = new byte[bitmapSize];

            ByteArrayOutputStream recordData = new ByteArrayOutputStream();

            for (int i = 0; i < record.data.size(); i++) {
                DataType dataType = record.data.get(i);

                // if null, set bit in null bitmap
                // don't write any data
                if (dataType.isNull()) {
                    setBit(bitmap, i, 1);
                    continue;
                }

                if (dataType instanceof DTVarchar) {
                    byte[] varcharData = dataType.convertToBytes();

                    // for varchar, must write 1 byte for the length
                    recordData.write((char) varcharData.length);
                    recordData.writeBytes(varcharData);
                }
                else {
                    recordData.writeBytes(dataType.convertToBytes());
                }

            }

            pageData.writeBytes(bitmap);
            pageData.writeBytes(recordData.toByteArray());

        }

        // TODO pad page using page size?

        return pageData.toByteArray();
    }

}