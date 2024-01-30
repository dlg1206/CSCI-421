package cli.catalog;

import java.io.Serializable;

public class Attribute implements Serializable {
    public enum DataTypes {
        INTEGER,
        DOUBLE,
        BOOLEAN,
        CHAR,
        VARCHAR
    }

    public DataTypes DataType;
    public int DataLength;
    public boolean Unique = false;
    public boolean NotNull = false;
    public boolean PrimaryKey = false;

}
