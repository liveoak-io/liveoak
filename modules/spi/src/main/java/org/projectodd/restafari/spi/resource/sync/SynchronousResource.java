package org.projectodd.restafari.spi.resource.sync;

import org.projectodd.restafari.spi.DeleteNotSupportedException;
import org.projectodd.restafari.spi.ReadNotSupportedException;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.ResourceException;
import org.projectodd.restafari.spi.resource.BlockingResource;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.Responder;

/**
 * @author Bob McWhirter
 */
public interface SynchronousResource extends Resource, BlockingResource {
    Resource read(RequestContext ctx, String id) throws ResourceException;
    void delete(RequestContext ctx) throws ResourceException;

    @Override
    default void read(RequestContext ctx, String id, Responder responder) {
        try {
            Resource result = read(ctx, id);
            responder.resourceRead(result);
        } catch (ReadNotSupportedException e) {
            responder.readNotSupported( this );
        } catch (Exception e) {
            // TODO be more specific
            responder.readNotSupported( this );
        }
    }

    @Override
    default void delete(RequestContext ctx, Responder responder) {
        try {
            delete(ctx);
        } catch (DeleteNotSupportedException e) {
            responder.deleteNotSupported( this );
        } catch (Exception e) {
            // TODO be more specific
            responder.deleteNotSupported( this );
        }
    }
}
