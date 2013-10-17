package org.projectodd.restafari.spi.synch;

import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.Responder;

import java.util.concurrent.Executor;

/**
 * @author Bob McWhirter
 */
public abstract class SynchronousResource implements Resource {

    public SynchronousResource() {

    }

    public SynchronousResource(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public void read(String id, Responder responder) {
        this.executor.execute(() -> {
            try {
                Resource result = read(id);
                responder.resourceRead(result);
            } catch (Exception e) {
                responder.noSuchResource(id);
            }
        });
    }

    public abstract Resource read(String id) throws Exception;


    @Override
    public void delete(Responder responder) {
        this.executor.execute(() -> {
            try {
                delete();
                responder.resourceDeleted( this );
            } catch (Exception e) {
                responder.deleteNotSupported( this );
            }
        });
    }

    public abstract void delete() throws Exception;


    private Executor executor;
    private Resource parent;
}
