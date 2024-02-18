package catalog;

import dataTypes.AttributeType;

public class Attribute implements IAttribute {
    private final String Name;
    private final AttributeType Type;
    private Integer MaxDataLength;
    private final boolean Unique;
    private final boolean Nullable;
    private final boolean PrimaryKey;

    public Attribute(String name, String type, boolean unique, boolean nullable, boolean primaryKey) {
        Name = name;
        Type = AttributeType.valueOf(type.toUpperCase());
        Unique = unique;
        Nullable = nullable;
        PrimaryKey = primaryKey;
    }

    public Attribute(String name, AttributeType type, boolean unique, boolean nullable, boolean primaryKey) {
        Name = name;
        Type = type;
        Unique = unique;
        Nullable = nullable;
        PrimaryKey = primaryKey;
    }

    public Attribute(String name, String type, int maxDataLength, boolean unique, boolean nullable, boolean primaryKey) {
        this(name, type, unique, nullable, primaryKey);
        MaxDataLength = maxDataLength;
    }

    public Attribute(String name, AttributeType type, int maxDataLength, boolean unique, boolean nullable, boolean primaryKey) {
        this(name, type, unique, nullable, primaryKey);
        MaxDataLength = maxDataLength;
    }

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public AttributeType getDataType() {
        return Type;
    }

    @Override
    public boolean isNullable() {
        return Nullable;
    }

    @Override
    public int getMaxDataLength() {
        return MaxDataLength;
    }

    @Override
    public boolean isUnique() {
        return Unique;
    }

    @Override
    public boolean isPrimaryKey() {
        return PrimaryKey;
    }
}
