package dataTypes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TestDataTypes {
    public static void main(String args[]) {
        ArrayList<DataType> data = new ArrayList<DataType>();

        data.add(new DTBoolean("True"));
        data.add(new DTBoolean("1")); // gives False
        data.add(new DTInteger("5"));
        data.add(new DTDouble("10.0"));
        data.add(new DTChar("Hello"));
        data.add(new DTVarchar("World"));

        // print out values
        System.out.println("Print Original Values");
        for (DataType d: data) {
            d.printValue();
        }


        // test converting to byte arrays and writing bytes to file
        // note: this is only to test how writing to file could be handled but is not exactly how the data
        // will be written for the project
        try (FileOutputStream fos = new FileOutputStream("testDataTypes")) {
            for (DataType d: data) {
                    fos.write(d.convertToBytes());
                    fos.write('\n');
            }
        } catch (IOException e) {
            System.err.println("Error writing to the file: " + e.getMessage());
        }


        // test converting to byte arrays and back to original data type
        ArrayList<DataType> newData = new ArrayList<DataType>();
        for (DataType d: data) {
            byte[] bytes = d.convertToBytes();

            if (d instanceof DTBoolean) {
                newData.add(new DTBoolean(bytes));
            } else if (d instanceof DTInteger) {
                newData.add(new DTInteger(bytes));
            } else if (d instanceof DTDouble) {
                newData.add(new DTDouble(bytes));
            } else if (d instanceof DTChar) {
                newData.add(new DTChar(bytes));
            } else if (d instanceof DTVarchar) {
                newData.add(new DTVarchar(bytes));
            }
        }

        // print out values of new data (should match original values)
        System.out.println("\nPrint New Values");
        for (DataType d: newData) {
            d.printValue();
        }

    }

}