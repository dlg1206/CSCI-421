package catalog;

import cli.cmd.exception.ExecutionFailure;
import cli.util.Console;
import dataTypes.*;
import sm.StorageManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Catalog implements ICatalog {

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
            new Attribute("order", AttributeType.INTEGER, false, false),
            new Attribute("unique", AttributeType.BOOLEAN, false, false),
            new Attribute("nullable", AttributeType.BOOLEAN, false, false),
            new Attribute("primarykey", AttributeType.BOOLEAN, false, false)
    );

    private static final int ORDER_INDEX = 4;

    private int PageSize;
    private final int BufferSize;
    private final String DBPath;
    private final Map<String, Table> Tables = new HashMap<>();
    private int NextNum = 1;
    public StorageManager StorageManager;

    public Catalog(int pageSize, int bufferSize, String DBPath) {
        this.PageSize = pageSize;
        this.BufferSize = bufferSize;
        this.DBPath = DBPath;
        initStorageManager();
    }

    private void initStorageManager() {
        Path pageSizePath = Paths.get(this.DBPath, PAGE_SIZE_PATH + ".int");
        if (pageSizePath.toFile().exists()) {
            try {
                loadOldDB(pageSizePath);
            } catch (Exception e) {
                System.out.println(Console.RED + "The db has become corrupt." + Console.RESET);
                System.exit(-2);
            }
        } else {
            createNewDB(pageSizePath);
        }
    }

    private void createNewDB(Path pageSizePath) {
        try {
            DTInteger pageSizeInt = new DTInteger(Objects.toString(this.PageSize));
            Files.write(pageSizePath, pageSizeInt.convertToBytes());
        } catch (IOException ioe) {
            System.out.println(Console.RED + "The db location is unusable." + Console.RESET);
            System.exit(-2);
        }
        this.StorageManager = new StorageManager(this.BufferSize, this.PageSize, this.DBPath);
    }

    private void loadOldDB(Path pageSizePath) throws ExecutionFailure {
        try {
            this.PageSize = new DTInteger(Files.readAllBytes(pageSizePath)).getValue();
        } catch (IOException ioe) {
            System.out.println(Console.RED + "The db has become corrupt." + Console.RESET);
            System.exit(-1);
        }
        this.StorageManager = new StorageManager(this.BufferSize, this.PageSize, this.DBPath);

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
                boolean unique = ((DTBoolean) record.get(5)).getValue();
                boolean nullable = ((DTBoolean) record.get(6)).getValue();
                boolean primarykey = ((DTBoolean) record.get(7)).getValue();

                tableObjects.get(table_id).addAttribute(new Attribute(name, type, unique, nullable, primarykey));
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

        List<List<DataType>> allTables = StorageManager.getAllRecords(TABLE_DATA_NUM, TABLE_SCHEMA);

        for (Attribute a : attributes) {
            addAttribute(name, a);
        }

        NextNum++;
    }

    @Override
    public int getPageSize() {
        return PageSize;
    }

    @Override
    public int getBufferSie() {
        return BufferSize;
    }

    @Override
    public Set<String> getExistingTableNames() {
        return Tables.keySet();
    }

    @Override
    public int getTableNumber(String name) {
        return Tables.get(name).getNumber();
    }

    @Override
    public Table getRecordSchema(String tableName) {
        return Tables.get(tableName);
    }


    public void deleteTable(String name) throws ExecutionFailure, IOException {
        Table t = Tables.remove(name);


        //TODO: Actually use a proper delete method, this ain't gonna work
        List<List<DataType>> allTables = StorageManager.getAllRecords(TABLE_DATA_NUM, TABLE_SCHEMA)
                .stream().filter(t1 -> ((DTInteger) t1.get(0)).getValue() != t.getNumber()).toList();

        StorageManager.dropTable(TABLE_DATA_NUM);
        for (List<DataType> table : allTables) {
            StorageManager.insertRecord(TABLE_DATA_NUM, TABLE_SCHEMA, table);
        }


        List<List<DataType>> allAttributes = StorageManager.getAllRecords(ATTR_DATA_NUM, ATTR_SCHEMA)
                .stream().filter(t1 -> ((DTInteger) t1.get(1)).getValue() != t.getNumber()).toList();

        StorageManager.dropTable(ATTR_DATA_NUM);
        for (List<DataType> attr : allAttributes) {
            StorageManager.insertRecord(ATTR_DATA_NUM, ATTR_SCHEMA, attr);
        }

    }

    @Override
    public void addAttribute(String tableName, Attribute attribute) throws ExecutionFailure, IOException {
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

        List<DataType> record = List.of(
                new DTInteger(Objects.toString(nextId)),
                new DTInteger(Objects.toString(t.getNumber())),
                new DTVarchar(attribute.getName()),
                new DTVarchar(attribute.getDataType().name()),
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
            return r1.get(ORDER_INDEX).compareTo(r2.get(ORDER_INDEX));
        }
    }
}
