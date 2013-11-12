package org.projectodd.restafari.spi;

/**
 * @author Bob McWhirter
 */
public class ResourceNotFoundException extends ResourceException {

    public ResourceNotFoundException(String path) {
        super(path, "Resource with id '" + path + "' not found" );
    }
}
