package org.projectodd.restafari.spi;

import org.projectodd.restafari.spi.resource.Resource;

/**
 * @author Bob McWhirter
 */
public class ReadNotSupportedException extends ResourceException {

    public ReadNotSupportedException(String path) {
        super(path, "Read not supported for '" + path + "'" );
    }
}
