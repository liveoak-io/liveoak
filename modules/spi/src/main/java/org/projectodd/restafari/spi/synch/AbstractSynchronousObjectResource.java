package org.projectodd.restafari.spi.synch;

import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.state.ObjectResourceState;

import java.util.stream.Stream;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractSynchronousObjectResource extends AbstractSynchronousResource implements ObjectResource, SynchronousObjectResource  {

    @Override
    public final void update(ObjectResourceState state, Responder responder) {
        this.executor.execute(() -> {
            try {
                update(state);
                responder.resourceUpdated(this);
            } catch (Exception e) {
                responder.updateNotSupported(this);
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
