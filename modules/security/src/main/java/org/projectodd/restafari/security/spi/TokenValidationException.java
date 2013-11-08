package org.projectodd.restafari.security.spi;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TokenValidationException extends Exception {

    public TokenValidationException(String message) {
        super(message);
    }

    public TokenValidationException(String message, Exception cause) {
        super(message,cause);
    }
}
