package catalog;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Table implements ITable {

    private final String Name;
    private final int Number;
    private final Map<String, Attribute> Attributes;

    public Table(String name, int number, List<Attribute> attributes) {
        Name = name;
        Attributes = attributes.stream()
                .collect(Collectors.toMap(Attribute::getName, Function.identity()));
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
        return Attributes.values().stream().toList();
    }

    @Override
    public void addAttribute(Attribute attribute) {
        Attributes.put(attribute.getName(), attribute);
    }

    @Override
    public void removeAttribute(String name) {
        Attributes.remove(name);
    }
}
