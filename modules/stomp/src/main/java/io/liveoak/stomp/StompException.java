package io.liveoak.stomp;

/**
 * @author Bob McWhirter
 */
public class StompException extends Exception {

    public StompException(String message) {
        super(message);
    }

    public StompException(Throwable cause) {
        super(cause);
    }
}
