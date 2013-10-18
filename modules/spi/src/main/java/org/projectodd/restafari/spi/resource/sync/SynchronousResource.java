package org.projectodd.restafari.spi.resource.sync;

import org.projectodd.restafari.spi.resource.BlockingResource;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.Responder;

/**
 * @author Bob McWhirter
 */
public interface SynchronousResource extends Resource, BlockingResource {
    Resource read(String id) throws Exception;
    void delete() throws Exception;

    @Override
    default void read(String id, Responder responder) {
        try {
            Resource result = read(id);
            responder.resourceRead(result);
        } catch (Exception e) {
            responder.readNotSupported(this);
        }
    }

    @Override
    default void delete(Responder responder) {
        try {
            delete();
        } catch (Exception e) {
            responder.deleteNotSupported( this );
        }
    }
}
