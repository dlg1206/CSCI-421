package catalog;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class Table implements ITable {

    private final String Name;
    private final int Number;
    private final List<Attribute> Attributes;

    public Table(String name, int number, List<Attribute> attributes) {
        Name = name;
        Attributes = attributes;
        Number = number;
    }


    Table(String name, int number) {
        this(name, number, new ArrayList<>());
    }

    @Override
    public int getNumber() {
        return Number;
    }

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public List<Attribute> getAttributes() {
        return Attributes;
    }

    @Override
    public Attribute getAttribute(String attrName) {
        return Attributes.stream().filter(a -> Objects.equals(a.getName(), attrName)).findFirst().orElseThrow();
    }

    @Override
    public void addAttribute(Attribute attribute) {
        // This does not check for unique attribute names, that should be handled by the command.
        Attributes.add(attribute);
    }

    public int getIndexOfPrimaryKey() {
        for (int i = 0; i < Attributes.size(); i++) {
            if (Attributes.get(i).isPrimaryKey())
                return i;
        }
        return -1; // Should never happen. All tables should have a PK.
    }

    public int getIndexOfAttribute(String attrName) {
        for (int i = 0; i < Attributes.size(); i++) {
            if (Attributes.get(i).getName().equals(attrName))
                return i;
        }
        return -1; // Should never happen. Never call this function without already validating the name exists.
    }
}
