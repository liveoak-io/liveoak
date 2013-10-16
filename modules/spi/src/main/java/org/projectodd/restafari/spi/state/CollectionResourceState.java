package org.projectodd.restafari.spi.state;

import java.util.stream.Stream;

/** Resource state representing a collection of resources.
 *
 * @author Bob McWhirter
 */
public interface CollectionResourceState extends ResourceState {

    /** Retrieve the member states.
     *
     * @return A stream providing member states.
     */
    Stream<? extends ResourceState> members();
}
