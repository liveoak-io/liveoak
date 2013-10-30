package org.projectodd.restafari.spi.resource.async;

import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;

import java.util.function.Consumer;

/** A sink to accept children of a resource when reading.
 *
 * @author Bob McWhirter
 */
public interface ResourceSink extends Consumer<Resource>, AutoCloseable {

    RequestContext requestContext();

    void close();
}
