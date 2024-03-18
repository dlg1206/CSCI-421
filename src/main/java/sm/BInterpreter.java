package sm;

import catalog.Attribute;
import catalog.NotSupportedConstraint;
import dataTypes.*;
import dataTypes.DataType;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <b>File:</b> BInterpreter.java
 * <p>
 * <b>Description:</b> Convert tables to/from binary
 *
 * @author Ryan Nowak
 */
public class BInterpreter {

    /**
     * Converts binary page data into DataType objects
     *
     * @param data binary page data
     * @param attributes table attributes
     * @return list of lists of DataTypes representing all the records in a page
     */
    public static List<List<DataType>> convertPageToRecords(byte[] data, List<Attribute> attributes) {
        List<List<DataType>> records = new ArrayList<>();

        ByteBuffer numRecBuff = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4));
        int numRecords = numRecBuff.getInt();
        int dataIdx = 4; // skip indexes 0-3 which contains number of records

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
                            int length = attributes.get(j).getMaxDataLength(); // get max length of char

                            dataTypes.add(new DTChar(Arrays.copyOfRange(data, dataIdx, dataIdx + length), length));
                            dataIdx += length;
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

            records.add(dataTypes);
        }

        return records;
    }

    /**
     * Convert DataType objects representing records into binary page data
     *
     * @param records list of lists of DataTypes representing all the records in a page
     * @return binary page data
     */
    public static byte[] convertRecordsToPage(List<List<DataType>> records) {
        ByteArrayOutputStream pageData = new ByteArrayOutputStream();

        // write number of records (4 bytes)
        pageData.writeBytes(ByteBuffer.allocate(4).putInt(records.size()).array());

        int bitmapSize = ((records.get(0).size()-1) / 8) + 1; // allocate bytes based on number of attributes

        // write each record
        for (List<DataType> record: records) {
            byte[] bitmap = new byte[bitmapSize];

            ByteArrayOutputStream recordData = new ByteArrayOutputStream();

            for (int i = 0; i < record.size(); i++) {
                DataType dataType = record.get(i);

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

        return pageData.toByteArray();
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
}
