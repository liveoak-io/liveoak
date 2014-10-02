package io.liveoak.client;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import io.liveoak.common.DefaultResourceRequest;
import io.liveoak.spi.exceptions.CreateNotSupportedException;
import io.liveoak.spi.exceptions.DeleteNotSupportedException;
import io.liveoak.spi.exceptions.NotAcceptableException;
import io.liveoak.spi.exceptions.NotAuthorizedException;
import io.liveoak.spi.exceptions.ReadNotSupportedException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.exceptions.ResourceAlreadyExistsException;
import io.liveoak.spi.exceptions.ResourceException;
import io.liveoak.spi.exceptions.ResourceNotFoundException;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.exceptions.UpdateNotSupportedException;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.state.ResourceState;
import io.netty.channel.local.LocalAddress;
import org.jboss.logging.Logger;

/**
 * @author Bob McWhirter
 */
public class DefaultClient implements Client {

    public DefaultClient() {

    }

    //@Override
    public void connect(SocketAddress address) throws Exception {
        log.debug("connect local client");
        this.connection = new LocalConnection(this);
        this.connection.connect(address);
    }

    //@Override
    public void close() {
        this.connection.close();
    }

    /**
     * Creates a new client to be used with synchronous calls.
     *
     * Note: A new client is required for synchronous calls since since
     * if a synchronous call occurs within an asynchronous one it will block.
     *
     * @return A new client for performing synchronous calls
     * @throws Exception
     */
    private DefaultClient createSyncClient() throws Exception {
        DefaultClient syncClient = new DefaultClient();
        syncClient.connect(new LocalAddress("liveoak"));

        return syncClient;
    }

    /**
     * Perform an asynchronous CREATE action.
     *
     * @param context The request context.
     * @param path    The path portion of the resource's URI.
     * @param state   The inbound state to use during creation.
     * @param handler Asynchronously result handler.
     */
    @Override
    public void create(RequestContext context, String path, ResourceState state, Consumer<ClientResourceResponse> handler) {
        ResourceRequest request = new DefaultResourceRequest.Builder(RequestType.CREATE, new ResourcePath(path))
                .resourceState(state)
                .requestContext(context)
                .build();
        this.connection.write(new ClientRequest(request, handler));
    }

    /**
     * Perform a synchronous CREATE action.
     *
     * @param context The request context.
     * @param path    The path portion of the resource's URI.
     * @param state   The inbound state to use during creation.
     * @return The resulting state of the request.
     * @throws ResourceException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public ResourceState create(RequestContext context, String path, ResourceState state) throws Exception {
        CompletableFuture<ResourceState> future = new CompletableFuture<>();

        DefaultClient syncClient = createSyncClient();
        syncClient.create(context, path, state, (response) -> {
            if (response.responseType() == ClientResourceResponse.ResponseType.OK) {
                future.complete(response.state());
            } else {
                handleError(response, future);
            }
        });

        try {
            ResourceState resourceState = future.get();
            syncClient.close();
            return resourceState;
        } catch (ExecutionException e) {
            e.getCause().fillInStackTrace();
            if (e.getCause() instanceof ResourceException) {
                throw (ResourceException) e.getCause();
            }
            throw e;
        }
    }

    /**
     * Perform an asynchronous READ action.
     *
     * @param context The request context.
     * @param path    The path portion of the resource's URI.
     * @param handler Asynchronously result handler.
     */
    @Override
    public void read(RequestContext context, String path, Consumer<ClientResourceResponse> handler) {
        ResourceRequest request = new DefaultResourceRequest.Builder(RequestType.READ, new ResourcePath(path))
                .requestContext(context)
                .build();
        this.connection.write(new ClientRequest(request, handler));
    }

    /**
     * Perform a synchronous READ action.
     *
     * @param context The request context.
     * @param path    The path portion of the resource's URI.
     * @return The resulting state of the request.
     * @throws ResourceException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public ResourceState read(RequestContext context, String path) throws Exception {
        CompletableFuture<ResourceState> future = new CompletableFuture<>();

        DefaultClient syncClient = createSyncClient();
        syncClient.read(context, path, (response) -> {
            if (response.responseType() == ClientResourceResponse.ResponseType.OK) {
                future.complete(response.state());
            } else {
                handleError(response, future);
            }
        });

        try {
            ResourceState resourceState = future.get();
            syncClient.close();
            return resourceState;
        } catch (ExecutionException e) {
            if (e.getCause() != null) {
                e.getCause().fillInStackTrace();
            }
            if (e.getCause() instanceof ResourceException) {
                throw (ResourceException) e.getCause();
            }
            throw e;
        }
    }

    /**
     * Perform an asynchronous UPDATE action.
     * <p>
     * <p>UPDATE has UPSERT semantics, in that if an attempt to
     * update a non-existant resource fails, an attempt is made
     * to create a resource at that location in the implied parent
     * container resource.</p>
     *
     * @param context The request context.
     * @param path    The path portion of the resource's URI.
     * @param state   The inbound state to use during update.
     * @param handler Asynchronously result handler.
     */
    @Override
    public void update(RequestContext context, String path, ResourceState state, Consumer<ClientResourceResponse> handler) {
        ResourceRequest request = new DefaultResourceRequest.Builder(RequestType.UPDATE, new ResourcePath(path))
                .resourceState(state)
                .requestContext(context)
                .build();
        this.connection.write(new ClientRequest(request, handler));
    }

    /**
     * Perform a synchronous UPDATE action.
     * <p>
     * <p>UPDATE has UPSERT semantics, in that if an attempt to
     * update a non-existant resource fails, an attempt is made
     * to create a resource at that location in the implied parent
     * container resource.</p>
     *
     * @param context The request context.
     * @param path    The path portion of the resource's URI.
     * @param state   The inbound state to use during creation.
     * @return The resulting state of the request.
     * @throws ResourceException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public ResourceState update(RequestContext context, String path, ResourceState state) throws Exception {
        CompletableFuture<ResourceState> future = new CompletableFuture<>();

        DefaultClient syncClient = createSyncClient();
        syncClient.update(context, path, state, (response) -> {
            if (response.responseType() == ClientResourceResponse.ResponseType.OK) {
                future.complete(response.state());
            } else {
                handleError(response, future);
            }
        });

        try {
            ResourceState resourceState = future.get();
            syncClient.close();
            return resourceState;
        } catch (ExecutionException e) {
            e.getCause().fillInStackTrace();
            if (e.getCause() instanceof ResourceException) {
                throw (ResourceException) e.getCause();
            }
            throw e;
        }
    }

    /**
     * Perform an asynchronous DELETE action.
     *
     * @param context The request context.
     * @param path    The path portion of the resource's URI.
     * @param handler Asynchronously result handler.
     */
    @Override
    public void delete(RequestContext context, String path, Consumer<ClientResourceResponse> handler) {
        ResourceRequest request = new DefaultResourceRequest.Builder(RequestType.DELETE, new ResourcePath(path))
                .requestContext(context)
                .build();
        this.connection.write(new ClientRequest(request, handler));
    }

    /**
     * Perform a synchronous DELTE action.
     *
     * @param context The request context.
     * @param path    The path portion of the resource's URI.
     * @return The resulting state of the request.
     * @throws ResourceException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public ResourceState delete(RequestContext context, String path) throws Exception {
        CompletableFuture<ResourceState> future = new CompletableFuture<>();

        DefaultClient syncClient = createSyncClient();
        syncClient.delete(context, path, (response) -> {
            if (response.responseType() == ClientResourceResponse.ResponseType.OK) {
                future.complete(response.state());
            } else {
                handleError(response, future);
            }
        });

        try {
            ResourceState resourceState = future.get();
            syncClient.close();
            return resourceState;
        } catch (ExecutionException e) {
            e.getCause().fillInStackTrace();
            if (e.getCause() instanceof ResourceException) {
                throw (ResourceException) e.getCause();
            }
            throw e;
        }
    }

    void handleError(ClientResourceResponse response, CompletableFuture<?> future) {
        switch (response.responseType()) {
            case NOT_AUTHORIZED:
                future.completeExceptionally(new NotAuthorizedException(response.path()));
                break;
            case NOT_ACCEPTABLE:
                future.completeExceptionally(new NotAcceptableException(response.path()));
                break;
            case NO_SUCH_RESOURCE:
                future.completeExceptionally(new ResourceNotFoundException(response.path()));
                break;
            case RESOURCE_ALREADY_EXISTS:
                future.completeExceptionally(new ResourceAlreadyExistsException(response.path()));
                break;
            case CREATE_NOT_SUPPORTED:
                future.completeExceptionally(new CreateNotSupportedException(response.path()));
                break;
            case READ_NOT_SUPPORTED:
                future.completeExceptionally(new ReadNotSupportedException(response.path()));
                break;
            case UPDATE_NOT_SUPPORTED:
                future.completeExceptionally(new UpdateNotSupportedException(response.path()));
                break;
            case DELETE_NOT_SUPPORTED:
                future.completeExceptionally(new DeleteNotSupportedException(response.path()));
                break;
            case INTERNAL_ERROR:
                future.completeExceptionally(new ResourceException("internal error"));
                break;
        }
    }

    private Connection connection;

    private static final Logger log = Logger.getLogger(DefaultClient.class);
}
