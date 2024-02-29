package mocks;

/**
 * <b>File:</b> StdoutBuilder.java
 * <p>
 * <b>Description:</b>
 *
 * @author Derek Garcia
 */
public class MockStdoutBuilder {

    private final StringBuilder sb = new StringBuilder();

    public MockStdoutBuilder addLine(String line) {
        this.sb.append(line).append('\n');
        return this;
    }

    public MockStdoutBuilder skipLine() {
        this.sb.append('\n');
        return this;
    }

    public String build() {
        return this.sb.toString();
    }
}
