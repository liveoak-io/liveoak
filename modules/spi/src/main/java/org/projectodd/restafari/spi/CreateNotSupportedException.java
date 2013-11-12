package org.projectodd.restafari.spi;

/**
 * @author Bob McWhirter
 */
public class CreateNotSupportedException extends ResourceException {

    public CreateNotSupportedException(String path) {
        super(path, "Create not supported for '" + path + "'");
    }

}
