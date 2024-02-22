package sm;

/**
 * <b>File:</b> SwapPage.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class SwapPage extends Page{
    /**
     * Create new Empty Page
     *
     * @param writeFile
     * @param pageSize   Max number of records page can hold
     * @param pageNumber Page Number
     * @param data
     */
    public SwapPage(TableFile writeFile, int pageSize, int pageNumber, byte[] data) {
        super(writeFile, pageSize, pageNumber, data);
    }
}
