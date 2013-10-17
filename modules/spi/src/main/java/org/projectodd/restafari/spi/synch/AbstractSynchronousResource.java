package org.projectodd.restafari.spi.synch;

import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.Responder;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractSynchronousResource implements SynchronousResource, Resource {

    public AbstractSynchronousResource() {

    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public final void read(String id, Responder responder) {
        this.executor.execute(() -> {
            try {
                Resource result = read(id);
                responder.resourceRead(result);
            } catch (Exception e) {
                responder.noSuchResource(id);
            }
        });
    }

    @Override
    public final void delete(Responder responder) {
        this.executor.execute(() -> {
            try {
                delete();
                responder.resourceDeleted( this );
            } catch (Exception e) {
                responder.deleteNotSupported( this );
            }
        });
    }

    protected Executor executor;
}
