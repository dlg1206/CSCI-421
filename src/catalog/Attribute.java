package catalog;

import dataTypes.AttributeType;

public class Attribute implements IAttribute {
    private final String Name;
    private final AttributeType Type;
    private Integer MaxDataLength;
    private final boolean Unique;
    private final boolean Nullable;
    private final boolean PrimaryKey;

    private Attribute(String name, AttributeType type, boolean unique, boolean nullable, boolean primaryKey) {
        Name = name;
        Type = type;
        Unique = unique;
        Nullable = nullable;
        PrimaryKey = primaryKey;
    }

    Attribute(String name, String type, boolean unique, boolean nullable, boolean primaryKey) {
        this(name, AttributeType.valueOf(type.toUpperCase()), unique, nullable, primaryKey);
    }

    Attribute(String name, String type, Integer maxDataLength, boolean unique, boolean nullable, boolean primaryKey) {
        this(name, AttributeType.valueOf(type.toUpperCase()), unique, nullable, primaryKey);
        MaxDataLength = maxDataLength;
    }

    public Attribute(String name, String type, boolean unique, boolean nullable) {
        this(name, AttributeType.valueOf(type.toUpperCase()), unique, nullable, false);
    }

    public Attribute(String name, AttributeType type, boolean unique, boolean nullable) {
        this(name, type, unique, nullable, false);
    }

    public Attribute(String name, String type, int maxDataLength, boolean unique, boolean nullable) {
        this(name, AttributeType.valueOf(type.toUpperCase()), unique, nullable, false);
        MaxDataLength = maxDataLength;
    }

    public Attribute(String name, AttributeType type, int maxDataLength, boolean unique, boolean nullable) {
        this(name, type, unique, nullable, false);
        MaxDataLength = maxDataLength;
    }

    public Attribute(String name, String type) {
        this(name, AttributeType.valueOf(type.toUpperCase()), true, false, true);
    }

    public Attribute(String name, AttributeType type) {
        this(name, type, true, false, true);
    }

    public Attribute(String name, String type, int maxDataLength) {
        this(name, AttributeType.valueOf(type.toUpperCase()), true, false, true);
        MaxDataLength = maxDataLength;
    }

    public Attribute(String name, AttributeType type, int maxDataLength) {
        this(name, type, true, false, true);
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
