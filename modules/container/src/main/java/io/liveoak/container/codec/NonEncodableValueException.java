package io.liveoak.container.codec;

/**
 * @author Bob McWhirter
 */
public class NonEncodableValueException extends Exception {

    public NonEncodableValueException(Object value) {
        super( "Unable to encode: " + value );
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    private Object value;
}
