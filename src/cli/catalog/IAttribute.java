package cli.catalog;

public interface IAttribute {
    String getName();
    String getDataType();
    boolean isNullable();
    int getMaxDataLength() throws NotSupportedConstraint;
    boolean isUnique();
    boolean isPrimaryKey();
}
