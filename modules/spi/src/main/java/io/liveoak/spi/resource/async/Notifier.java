package io.liveoak.spi.resource.async;

import io.liveoak.spi.ResourceResponse;

/**
 * Notifier to allow resources to information the container
 * of changes that occur outside of a request/response cycle.
 *
 * @author Bob McWhirter
 * @see Responder
 */
public interface Notifier {

    /**
     * Notify the container of a newly-created resource.
     *
     * @param resourceResponse The response from the created resource.
     */
    void resourceCreated(ResourceResponse resourceResponse);

    /**
     * Notify the container of a deleted resource.
     *
     * @param resourceResponse The response from the deleted resource.
     */
    void resourceDeleted(ResourceResponse resourceResponse);

    /**
     * Notify the container of an updated resource.
     *
     * @param resourceResponse The response from updated resource.
     */
    void resourceUpdated(ResourceResponse resourceResponse);

}
