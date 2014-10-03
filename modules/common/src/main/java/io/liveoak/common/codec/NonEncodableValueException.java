package io.liveoak.common.codec;

/**
 * @author Bob McWhirter
 */
public class NonEncodableValueException extends RuntimeException {

    public NonEncodableValueException(Object value) {
        super("Unable to encode: " + value);
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    private Object value;
}
