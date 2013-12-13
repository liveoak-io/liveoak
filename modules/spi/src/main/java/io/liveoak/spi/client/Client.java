package io.liveoak.spi.client;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceException;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public interface Client {
    //void connect() throws Exception;

    //void close();

    void create(RequestContext context, String path, ResourceState state, Consumer<ClientResourceResponse> handler);

    ResourceState create(RequestContext context, String path, ResourceState state) throws ResourceException, ExecutionException, InterruptedException;

    void read(RequestContext context, String path, Consumer<ClientResourceResponse> handler);

    ResourceState read(RequestContext context, String path) throws ResourceException, ExecutionException, InterruptedException;

    void update(RequestContext context, String path, ResourceState state, Consumer<ClientResourceResponse> handler);

    ResourceState update(RequestContext context, String path, ResourceState state) throws ResourceException, ExecutionException, InterruptedException;

    void delete(RequestContext context, String path, Consumer<ClientResourceResponse> handler);

    ResourceState delete(RequestContext context, String path) throws ResourceException, ExecutionException, InterruptedException;
}
