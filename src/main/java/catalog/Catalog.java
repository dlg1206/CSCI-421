package catalog;

import cli.cmd.exception.ExecutionFailure;
import util.Console;
import dataTypes.*;
import sm.StorageManager;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.*;

public class Catalog implements ICatalog {

    private static final String DB_SETTING_STR = "Using DB settings: Page size: %s, IsIndexed: %s.";
    private static final String CRIT_DELETE_ERROR_STR = "A critical error occurred while deleting the table from the catalog.";

    private static final String PAGE_SIZE_PATH = "ps";
    private static final int TABLE_DATA_NUM = Integer.MIN_VALUE;
    private static final List<Attribute> TABLE_SCHEMA = List.of(
            new Attribute("id", AttributeType.INTEGER),
            new Attribute("name", AttributeType.VARCHAR, 255, true, false)
    );
    private static final int ATTR_DATA_NUM = Integer.MIN_VALUE + 1;
    private static final List<Attribute> ATTR_SCHEMA = List.of(
            new Attribute("id", AttributeType.INTEGER),
            new Attribute("table_id", AttributeType.INTEGER, false, false),
            new Attribute("name", AttributeType.VARCHAR, 255, true, false),
            new Attribute("type", AttributeType.VARCHAR, 7, false, false),
            new Attribute("maxDataLen", AttributeType.INTEGER, false, true),
            new Attribute("order", AttributeType.INTEGER, false, false),
            new Attribute("unique", AttributeType.BOOLEAN, false, false),
            new Attribute("nullable", AttributeType.BOOLEAN, false, false),
            new Attribute("primarykey", AttributeType.BOOLEAN, false, false)
    );

    private static final int ORDER_INDEX = 5;

    private int PageSize;
    private final int BufferSize;
    private final String DBPath;
    private boolean IsIndexed;
    private final Map<String, Table> Tables = new HashMap<>();
    private int NextNum = 1;
    public StorageManager StorageManager;

    public Catalog(int pageSize, int bufferSize, String DBPath, boolean isIndexed) {
        this.PageSize = pageSize;
        this.BufferSize = bufferSize;
        this.DBPath = DBPath;
        this.IsIndexed = isIndexed;
        initStorageManager();
    }

    private void initStorageManager() {
        Path pageSizePath = Paths.get(this.DBPath, PAGE_SIZE_PATH + ".int");
        Console.out("Looking for an existing database...");
        if (pageSizePath.toFile().exists()) {
            Console.out("Existing database found, loading the old database...");
            try {
                loadOldDB(pageSizePath);
            } catch (Exception e) {
                Console.err("The db has become corrupt.");
                System.exit(-2);
            }
            Console.out("Old database loaded successfully!");
        } else {
            Console.out("None found, creating a new database...");
            createNewDB(pageSizePath);
            Console.out("New database created!");
        }
    }

    private void createNewDB(Path pageSizePath) {
        try {
            Files.createDirectories(Paths.get(this.DBPath));
            DTInteger pageSizeInt = new DTInteger(Objects.toString(this.PageSize));
            DTBoolean isIndexedBool = new DTBoolean(Objects.toString(this.IsIndexed));
            Files.write(pageSizePath, pageSizeInt.convertToBytes());
            Files.write(pageSizePath, isIndexedBool.convertToBytes(), StandardOpenOption.APPEND);
            Console.out(DB_SETTING_STR.formatted(this.PageSize, this.IsIndexed));
        } catch (IOException ioe) {
            Console.err("The db location is unusable.");
            System.exit(-2);
        }
        this.StorageManager = new StorageManager(this.BufferSize, this.PageSize, this.DBPath, this.IsIndexed);
    }

    private void loadOldDB(Path pageSizePath) throws ExecutionFailure {
        try {
            byte[] dbData = Files.readAllBytes(pageSizePath);
            this.PageSize = new DTInteger(dbData).getValue();

            byte[] pageSizeData = new byte[4];
            byte[] isIndexedData = new byte[1];
            try (RandomAccessFile raf = new RandomAccessFile(pageSizePath.toString(), "r")) {
                raf.read(pageSizeData, 0, 4);
                raf.read(isIndexedData, 0, 1);
            }
            this.PageSize = new DTInteger(pageSizeData).getValue();
            this.IsIndexed = new DTBoolean(isIndexedData).getValue();
            Console.out(DB_SETTING_STR.formatted(this.PageSize, this.IsIndexed));
        } catch (IOException ioe) {
            Console.err("The db has become corrupt.");
            System.exit(-1);
        }
        this.StorageManager = new StorageManager(this.BufferSize, this.PageSize, this.DBPath , this.IsIndexed);

        HashMap<Integer, Table> tableObjects = new HashMap<>();

        List<List<DataType>> allTableRecords = StorageManager.getAllRecords(TABLE_DATA_NUM, TABLE_SCHEMA);

        for (List<DataType> record : allTableRecords) {

            int id = ((DTInteger) record.get(0)).getValue();
            String name = ((DTVarchar) record.get(1)).getValue();

            Table t = new Table(name, id);
            Tables.put(name, t);
            tableObjects.put(id, t);
        }

        List<List<DataType>> allAttrDTs = StorageManager.getAllRecords(ATTR_DATA_NUM, ATTR_SCHEMA);

        for (int tableNum : tableObjects.keySet()) {
            for (List<DataType> record : allAttrDTs.stream().filter(r -> ((DTInteger) r.get(1)).getValue() == tableNum).sorted(new AttrRecordSorter()).toList()) {
                int table_id = ((DTInteger) record.get(1)).getValue();
                String name = ((DTVarchar) record.get(2)).getValue();
                String type = ((DTVarchar) record.get(3)).getValue();
                Integer maxDataLen = ((DTInteger) record.get(4)).getValue();
                boolean unique = ((DTBoolean) record.get(6)).getValue();
                boolean nullable = ((DTBoolean) record.get(7)).getValue();
                boolean primarykey = ((DTBoolean) record.get(8)).getValue();

                if (maxDataLen == null) {
                    tableObjects.get(table_id).addAttribute(new Attribute(name, type, unique, nullable, primarykey));
                } else {
                    tableObjects.get(table_id).addAttribute(new Attribute(name, type, maxDataLen, unique, nullable, primarykey));
                }
            }
        }

        NextNum = tableObjects.keySet().stream().max(Comparator.naturalOrder()).orElse(0) + 1;

    }

    public void createTable(String name, List<Attribute> attributes) throws IOException, ExecutionFailure {
        Tables.put(name, new Table(name, NextNum));

        List<DataType> record = List.of(
                new DTInteger(Objects.toString(NextNum)),
                new DTVarchar(name)
        );

        StorageManager.insertRecord(TABLE_DATA_NUM, TABLE_SCHEMA, record);

        for (Attribute a : attributes) {
            addAttribute(name, a);
        }

        StorageManager.flush();

        NextNum++;
    }

    @Override
    public int getPageSize() {
        return PageSize;
    }

    @Override
    public Set<String> getExistingTableNames() {
        return Tables.keySet();
    }

    @Override
    public int getTableNumber(String name) {
        name = name.toLowerCase();
        return Tables.get(name).getNumber();
    }

    @Override
    public Table getRecordSchema(String tableName) {
        tableName = tableName.toLowerCase();
        return Tables.get(tableName);
    }

    @Override
    public Attribute getTableAttribute(String tableName, String attrName) {
        return getRecordSchema(tableName).getAttribute(attrName);
    }


    public void deleteTable(String name) throws ExecutionFailure {
        name = name.toLowerCase();
        Table t = Tables.remove(name);

        try {
            // Delete the entry from the table data relation
            StorageManager.deleteRecord(TABLE_DATA_NUM, new DTInteger(Integer.toString(t.getNumber())), TABLE_SCHEMA);

            // Delete all related entries from the attribute data relation
            //      Note: I wish we could use StorageManager.selectRecords, but because the catalog does not know about
            //      the attr data relation (i.e., the relation does not have a "name"), it will not be able to build a
            //      where tree.
            for (List<DataType> record: StorageManager.getAllRecords(ATTR_DATA_NUM, ATTR_SCHEMA)) {
                if (((DTInteger) record.get(1)).getValue() == t.getNumber()) {
                    StorageManager.deleteRecord(ATTR_DATA_NUM, record.getFirst(), ATTR_SCHEMA);
                }
            }
        } catch (IOException e) {
            throw new ExecutionFailure(CRIT_DELETE_ERROR_STR);
        }
    }

    @Override
    public void addAttribute(String tableName, Attribute attribute) throws ExecutionFailure, IOException {
        tableName = tableName.toLowerCase();
        Table t = Tables.get(tableName);
        t.addAttribute(attribute);

        int nextId = StorageManager.getAllRecords(ATTR_DATA_NUM, ATTR_SCHEMA).stream()
                .map(a -> ((DTInteger) a.getFirst()).getValue())
                .max(Comparator.naturalOrder())
                .orElse(0)
                + 1;

        int nextOrder = StorageManager.getAllRecords(ATTR_DATA_NUM, ATTR_SCHEMA).stream()
                .filter(a -> ((DTInteger) a.get(1)).getValue() == t.getNumber())
                .map(a -> ((DTInteger) a.get(ORDER_INDEX)).getValue())
                .max(Comparator.naturalOrder())
                .orElse(0)
                + 1;

        DTInteger maxDataLen;

        if (attribute.getDataType() == AttributeType.CHAR || attribute.getDataType() == AttributeType.VARCHAR) {
            maxDataLen = new DTInteger(String.valueOf(attribute.getMaxDataLength()));
        } else {
            maxDataLen = new DTInteger((String) null);
        }

        List<DataType> record = List.of(
                new DTInteger(Objects.toString(nextId)),
                new DTInteger(Objects.toString(t.getNumber())),
                new DTVarchar(attribute.getName()),
                new DTVarchar(attribute.getDataType().name()),
                maxDataLen,
                new DTInteger(Objects.toString(nextOrder)),
                new DTBoolean(Objects.toString(attribute.isUnique())),
                new DTBoolean(Objects.toString(attribute.isNullable())),
                new DTBoolean(Objects.toString(attribute.isPrimaryKey()))
        );
        StorageManager.insertRecord(ATTR_DATA_NUM, ATTR_SCHEMA, record);
    }

    private static class AttrRecordSorter implements Comparator<List<DataType>> {
        @Override
        public int compare(List<DataType> r1, List<DataType> r2) {
            return r2.get(ORDER_INDEX).compareTo(r1.get(ORDER_INDEX)); // reverse the order
        }
    }
}
