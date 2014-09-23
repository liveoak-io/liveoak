package io.liveoak.spi;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class PropertyException extends Exception {
    public PropertyException() {
        super();
    }

    public PropertyException(String message) {
        super(message);
    }
}
