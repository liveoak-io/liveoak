package org.projectodd.restafari.spi;

import org.projectodd.restafari.spi.resource.Resource;

/**
 * @author Bob McWhirter
 */
public class DeleteNotSupportedException extends ResourceException {

    public DeleteNotSupportedException(String path) {
        super(path, "Delete not supported for '" + path + "'");
    }
}
