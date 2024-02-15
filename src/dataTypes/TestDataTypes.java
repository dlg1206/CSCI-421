package dataTypes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TestDataTypes {
    public static void main(String args[]) {
        ArrayList<DataType> data = new ArrayList<DataType>();

        data.add(new DTBoolean("True"));
        data.add(new DTBoolean("True")); // gives False
        data.add(new DTInteger("5"));
        data.add(new DTInteger("10"));
        data.add(new DTDouble("10.0"));
        data.add(new DTChar("Hello"));
        data.add(new DTVarchar("World"));

        // compare data
        // gives 0 if values are equal
        // 1 if val2 > val1
        // -1 if val2 < val1
        // note: if data types don't match, -1 is given
        // this shouldn't matter as we wouldn't compare different data types
        System.out.println("Compare Data");
        System.out.println(data.get(0).compareTo(data.get(1))); // 0, True == True
        System.out.println(data.get(2).compareTo(data.get(3))); // 1, 10 > 5
        System.out.println(data.get(3).compareTo(data.get(2))); // -1, 5 < 10
        System.out.println(data.get(5).compareTo(data.get(6))); // -1, data types don't match

        // print out values
        System.out.println("\nPrint Original Values");
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