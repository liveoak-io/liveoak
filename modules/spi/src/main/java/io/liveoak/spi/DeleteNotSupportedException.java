package io.liveoak.spi;

/**
 * @author Bob McWhirter
 */
public class DeleteNotSupportedException extends ResourceException {

    public DeleteNotSupportedException(String path) {
        super(path, "Delete not supported for '" + path + "'");
    }
}
