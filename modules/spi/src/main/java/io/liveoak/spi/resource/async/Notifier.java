package io.liveoak.spi.resource.async;

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
     * @param resource The created resource.
     */
    void resourceCreated(Resource resource);

    /**
     * Notify the container of a deleted resource.
     *
     * @param resource The deleted resource.
     */
    void resourceDeleted(Resource resource);

    /**
     * Notify the container of an updated resource.
     *
     * @param resource The updated resource.
     */
    void resourceUpdated(Resource resource);

}
