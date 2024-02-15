package catalog;

import java.util.List;

public interface ITable {
    int getNumber();
    String getName();
    List<Attribute> getAttributes();
    void addAttribute(Attribute attribute);
    void removeAttribute(String name);
}
