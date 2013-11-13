package io.liveoak.spi;

/**
 * @author Bob McWhirter
 */
public class NotAuthorizedException extends ResourceException {

    public NotAuthorizedException(String path) {
        super( path, "Not authorized for '" + path + "'" );
    }

    public NotAuthorizedException(String path, String message) {
        super( path, message );
    }
}
