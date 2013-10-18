package org.projectodd.restafari.spi;

import org.projectodd.restafari.spi.resource.Resource;

/**
 * @author Bob McWhirter
 */
public class ResourceNotFoundException extends ResourceException {

    public ResourceNotFoundException(String path) {
        super(path, "Resource with id '" + path + "' not found" );
    }
}
