package catalog;

import java.util.List;
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
    public void addAttribute(Attribute attribute) {
        // This does not check for unique attribute names, that should be handled by the command.
        Attributes.add(attribute);
    }

    @Override
    public void removeAttribute(String name) {
        Attributes.removeIf(a -> Objects.equals(a.getName(), name));
    }

    public int getIndexOfPrimaryKey() {
        for (int i = 0; i < Attributes.size(); i++) {
            if (Attributes.get(i).isPrimaryKey())
                return i;
        }
        return -1; // Should never happen. All tables should have a PK.
    }
}
