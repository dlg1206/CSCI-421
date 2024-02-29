package catalog;

import cli.cmd.exception.ExecutionFailure;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface ICatalog {

    Table getRecordSchema(String tableName);
    int getTableNumber(String name);
    int getPageSize();
    int getBufferSie();
    Set<String> getExistingTableNames();
    void createTable(String name, List<Attribute> attributes) throws IOException, ExecutionFailure;
    void deleteTable(String name) throws ExecutionFailure, IOException;
    void addAttribute(String tableName, Attribute attribute) throws ExecutionFailure, IOException;


}
