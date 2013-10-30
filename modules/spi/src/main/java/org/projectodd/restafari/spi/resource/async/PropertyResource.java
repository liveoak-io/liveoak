package org.projectodd.restafari.spi.resource.async;

import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.Resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * A simple key/value pair resource.
 *
 * <p>Typically used as members of an ObjectResource.</p>
 *
 * @author Bob McWhirter
 * @see ObjectResource
 */
public interface PropertyResource extends Resource {

    /**
     * Set the value.
     *
     * @param value The value.
     */
    void set(Object value);

    /**
     * Get the value.
     *
     * @return The value.
     */
    default Object get(RequestContext ctx) throws ExecutionException, InterruptedException {
        CompletableFuture<Object> future = new CompletableFuture<>();
        readContent(ctx, (val) -> {
            future.complete(val);
        });

        return future.get();
    }

    void readContent(RequestContext ctx, PropertyContentSink sink);
}
