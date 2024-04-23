package util.BPlusTree;

public class RecordPointer {
    public int pageNumber;
    public int index;

    public RecordPointer(int pageNumber, int index) {
        this.pageNumber = pageNumber;
        this.index = index;
    }

    @Override
    public String toString() {
        return "RecordPointer{pageNumber=" + pageNumber + ", index=" + index + '}';
    }
}