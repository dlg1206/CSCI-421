package catalog;

import java.util.List;
import java.util.Set;

public interface ICatalog {

    Table getRecordSchema(String tableName);
    int getTableNumber(String name);
    int getPageSize();
    int getBufferSie();
    Set<String> getExistingTableNames();
    void createTable(String name, List<Attribute> attributes);
    void deleteTable(String name);


}
