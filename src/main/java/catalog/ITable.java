package catalog;

import java.util.List;

public interface ITable {
    int getNumber();
    String getName();
    List<Attribute> getAttributes();
    Attribute getAttribute(String attrName);
    void addAttribute(Attribute attribute);
    int getIndexOfPrimaryKey();
}
