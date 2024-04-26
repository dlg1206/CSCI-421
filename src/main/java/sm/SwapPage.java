package sm;

/**
 * <b>File:</b> SwapPage.java
 * <p>
 * <b>Description:</b> Utility class to help distinguish between swap and regular pages
 *
 * @author Derek Garcia
 */
public class SwapPage extends Page {
    /**
     * Create new Swap Page
     *
     * @param writeFile  File to write page to
     * @param pageSize   Max number of records page can hold
     * @param pageNumber Page Number
     * @param data       Page byte data
     */
    public SwapPage(DBFile writeFile, int pageSize, int pageNumber, byte[] data) {
        super(writeFile, pageSize, pageNumber, data, false);
    }

    @Override
    public String toString() {
        return "SWAP: " + this.getWriteFile().getTableID() + ": " + getPageNumber();
    }
}
