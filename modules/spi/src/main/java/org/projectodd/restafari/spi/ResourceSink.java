package org.projectodd.restafari.spi;

import java.util.function.Consumer;

/** A sink to accept children of a resource when reading.
 *
 * @author Bob McWhirter
 */
public interface ResourceSink extends Consumer<Resource> {

    /** Indicate completion.
     */
    void close();
}
