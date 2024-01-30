package cli.catalog;

import java.io.Serializable;
import java.util.Map;

public class Table implements Serializable {

    public int Number;
    public Map<String, Attribute> Attributes;

    public Table(int number, Map<String, Attribute> attributes) {
        Attributes = attributes;
        Number = number;
    }

}
