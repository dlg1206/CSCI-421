package util.where;

import dataTypes.AttributeType;

public class InternalNode extends Node{

    final Node Left;
    final Node Right;
    String Comparator;
    private final AttributeType ReturnType = AttributeType.BOOLEAN;

    InternalNode(Node leftLeaf, Node rightLeaf, String comparator) {
        Left = leftLeaf;
        Right = rightLeaf;
        Comparator = comparator;
    }

    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public AttributeType getReturnType() {
        return ReturnType;
    }

    @Override
    public String toString() {
        return Left.toString() + " " + Comparator + " " + Right.toString();
    }

}
