package org.projectodd.restafari.spi;

/**
 * @author Bob McWhirter
 */
public class UpdateNotSupportedException extends ResourceException {

    public UpdateNotSupportedException(String path) {
        super(path, "Update not supported for '" + path + "'" );
    }
}
