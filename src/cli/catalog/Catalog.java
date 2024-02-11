package cli.catalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Catalog implements ICatalog {

    private final int PageSize;
    private final int BufferSize;
    private final Map<String, Table> Tables;
    private int NextNum = 1;
    private final String CatalogLocation; // TODO: Integrate with the storage manager

    public Catalog(int pageSize, int bufferSize, String catalogLocation) {
        Tables = new HashMap<>();
        PageSize = pageSize;
        BufferSize = bufferSize;
        CatalogLocation = catalogLocation;
        write();
    }

    public void createTable(String name, List<Attribute> attributes) {
        Tables.put(name, new Table(name, NextNum, attributes));
        NextNum++;
        write();
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

    public void deleteTable(String name) {
        Tables.remove(name);
        write();
    }

    public void write() {
        //TODO: Integrate with the storage manager
    }
}
