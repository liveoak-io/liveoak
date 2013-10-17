package org.projectodd.restafari.spi.synch;

import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractSynchronousCollectionResource extends AbstractSynchronousResource implements CollectionResource, SynchronousCollectionResource {

    @Override
    public void read(Pagination pagination, Responder responder) {
        if (pagination.getLimit() > 0 || pagination.getOffset() > 0) {
            this.executor.execute(() -> {
                responder.resourceRead( readPage( pagination ) );
            });
        } else {
            responder.resourceRead(this);
        }
    }

    @Override
    public void create(ResourceState state, Responder responder) {
        this.executor.execute(() -> {
            try {
                Resource result = create(state);
                responder.resourceCreated(result);
            } catch (Exception e) {
                responder.createNotSupported(this);
            }
        });
    }

    @Override
    public void writeMembers(ResourceSink sink) {
        this.executor.execute(() -> {
            members().forEach((m) -> {
                sink.accept(m);
            });
            sink.close();
        });
    }
}
