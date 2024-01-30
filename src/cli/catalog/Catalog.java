package cli.catalog;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Catalog implements Serializable {

    public int PageSize;
    public Map<String, Table> Tables;
    public int NextNum = 1;
    private final String CatalogLocation;


    public Catalog(int pageSize, String catalogLocation) {
        Tables = new HashMap<>();
        PageSize = pageSize;
        CatalogLocation = catalogLocation;
        write();
    }

    public void createTable(String name, Map<String, Attribute> attributes) {
        Tables.put(name, new Table(NextNum, attributes));
        NextNum++;
        write();
    }

    public Set<String> getExistingTableNames() {
        return Tables.keySet();
    }

    public int getTableNum(String name) {
        return Tables.get(name).Number;
    }

    public void deleteTable(String name) {
        Tables.remove(name);
        write();
    }

    public void write() {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(CatalogLocation);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(this);
        } catch (FileNotFoundException fnfe) {
            System.out.println("The catalog file could not be created. Quitting.");
            System.exit(1);
        } catch (IOException ioe) {
            System.out.println("The Object output stream could created for the catalog. Quitting.");
            System.exit(2);
        }
    }

    public static Catalog InitializeDB(String dbLoc, int pageSize) {
        String catalogPath = dbLoc + "catalog.db";
        File catalogLoc = new File(catalogPath);
        if (catalogLoc.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(catalogPath);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                return (Catalog) objectInputStream.readObject();
            } catch (FileNotFoundException fnfe) {
                System.out.println("The catalog file is missing. Quitting.");
                System.exit(3);
            } catch (IOException ioe) {
                System.out.println("The Object input stream could created for the catalog. Quitting.");
                System.out.println(ioe.getMessage());
                System.exit(4);
            } catch (Exception ignored){}
        }
        return new Catalog(pageSize, catalogPath);
    }

}
