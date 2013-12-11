/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.container;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceException;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Connector to the resoure-container that works in-VM, avoiding network traffic.
 *
 * <p>Inputs and outputs through the connector avoid all serialization to bytes
 * and work only in terms of {@link ResourceState} instances.</p>
 *
 * @author Bob McWhirter
 */
public interface DirectConnector {

    /**
     * Fetch an actual Resource component given its path.
     *
     * @param path The path of the resource.
     * @return The actual resource.
     * @throws ResourceException    If the resource cannot be found.
     * @throws ExecutionException   If an error occurs while asynchronously fetching.
     * @throws InterruptedException If interrupted before completing.
     */
    Resource fetch(String path) throws ResourceException, ExecutionException, InterruptedException;

    /**
     * Perform an asynchronous CREATE action.
     *
     * @param context The request context.
     * @param path    The path portion of the resource's URI.
     * @param state   The inbound state to use during creation.
     * @param handler Asynchronously result handler.
     */
    void create(RequestContext context, String path, ResourceState state, Consumer<ResourceResponse> handler);

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
    ResourceState create(RequestContext context, String path, ResourceState state) throws ResourceException, ExecutionException, InterruptedException;

    /**
     * Perform an asynchronous READ action.
     *
     * @param context The request context.
     * @param path    The path portion of the resource's URI.
     * @param handler Asynchronously result handler.
     */
    void read(RequestContext context, String path, Consumer<ResourceResponse> handler);

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
    ResourceState read(RequestContext context, String path) throws ResourceException, ExecutionException, InterruptedException;

    /**
     * Perform an asynchronous UPDATE action.
     *
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
    void update(RequestContext context, String path, ResourceState state, Consumer<ResourceResponse> handler);

    /**
     * Perform a synchronous UPDATE action.
     *
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
    ResourceState update(RequestContext context, String path, ResourceState state) throws ResourceException, ExecutionException, InterruptedException;

    /**
     * Perform an asynchronous DELETE action.
     *
     * @param context The request context.
     * @param path    The path portion of the resource's URI.
     * @param handler Asynchronously result handler.
     */
    void delete(RequestContext context, String path, Consumer<ResourceResponse> handler);

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
    ResourceState delete(RequestContext context, String path) throws ResourceException, ExecutionException, InterruptedException;

}
