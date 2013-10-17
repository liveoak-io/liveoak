package org.projectodd.restafari.spi.synch;

import org.projectodd.restafari.spi.PropertyResource;
import org.projectodd.restafari.spi.state.ObjectResourceState;

import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public interface SynchronousObjectResource extends SynchronousResource {
    void update(ObjectResourceState state) throws Exception;
    Stream<PropertyResource> members();
}
