package sm;

/**
 * <b>File:</b> Page.java
 * <p>
 * <b>Description:</b> Utility Page object to hold metadata about page
 *
 * @author Derek Garcia, Ryan Nowak
 */
class Page {
    private int tableID;
    private int number;
    private byte[] data;

    /**
     * Create new Page
     *
     * @param tableID Table ID of the page
     * @param number  Page Number
     * @param data    Raw binary data of the page
     */
    public Page(int tableID, int number, byte[] data) {
        this.tableID = tableID;
        this.number = number;
        this.data = data;
    }

    public int getTableID() {
        return this.tableID;
    }

    public int getNumber() {
        return this.number;
    }

    public byte[] getData() {
        return this.data;
    }


}