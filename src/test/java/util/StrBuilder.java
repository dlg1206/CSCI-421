package util;

/**
 * <b>File:</b> StrBuilder.java
 * <p>
 * <b>Description:</b> Utility String builder for creating diff matches from the stdout
 *
 * @author Derek Garcia
 */
public class StrBuilder {
    private final StringBuilder sb = new StringBuilder();

    /**
     * Add new line
     *
     * @param line Line to add
     * @return This Builder
     */
    public StrBuilder addLine(String line) {
        this.sb.append(line).append('\n');
        return this;
    }

    /**
     * Skip a line
     *
     * @return This Builder
     */
    public StrBuilder skipLine() {
        this.sb.append('\n');
        return this;
    }

    /**
     * @return The completed string stored in the builder
     */
    public String build() {
        return this.sb.toString();
    }
}
