package util;

/**
 * <b>File:</b> StdoutBuilder.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class StrBuilder {

    private final StringBuilder sb = new StringBuilder();

    public StrBuilder addLine(String line) {
        this.sb.append(line).append('\n');
        return this;
    }

    public StrBuilder skipLine() {
        this.sb.append('\n');
        return this;
    }

    public String build() {
        return this.sb.toString();
    }
}
