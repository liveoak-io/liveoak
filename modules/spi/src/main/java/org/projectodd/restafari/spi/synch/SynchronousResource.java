package org.projectodd.restafari.spi.synch;

import org.projectodd.restafari.spi.Resource;

/**
 * @author Bob McWhirter
 */
public interface SynchronousResource {
    Resource read(String id) throws Exception;
    void delete() throws Exception;
}
