package io.liveoak.spi.resource.async;

/**
 * @author Bob McWhirter
 */
public interface Notifier {

    void resourceCreated(Resource resource);

    void resourceDeleted(Resource resource);

    void resourceUpdated(Resource resource);

}
