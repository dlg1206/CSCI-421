package catalog;

import dataTypes.AttributeType;

public interface IAttribute {
    String getName();
    AttributeType getDataType();
    boolean isNullable();
    int getMaxDataLength() throws NotSupportedConstraint;
    boolean isUnique();
    boolean isPrimaryKey();
}
