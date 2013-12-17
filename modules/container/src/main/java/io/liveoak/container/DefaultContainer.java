/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.liveoak.spi.container.DeploymentException;
import io.liveoak.container.deploy.DirectoryDeploymentManager;
import io.liveoak.spi.Container;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.Deployer;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;


/**
 * Default resource-container implementation.
 *
 * @author Bob McWhirter
 */
public class DefaultContainer implements Container, Resource {

    /**
     * Construct a self-contained container.
     *
     * The container will instantiate its own Vertx platform.
     */
    public DefaultContainer() {
    }

    public void deployer(Deployer deployer) {
        this.deployer = deployer;
    }

    public Deployer deployer() {
        return this.deployer;
    }

    public void deploymentManager(DirectoryDeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    public DirectoryDeploymentManager deploymentManager() {
        return this.deploymentManager;
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {

    }


    // ----------------------------------------
    // ----------------------------------------

    public void registerResource(RootResource resource) {
        this.resources.put(resource.id(), resource);
    }

    public void unregisterResource(RootResource resource) {
        this.resources.remove(resource.id());
        resource.destroy();
    }

    @Override
    public boolean hasResource(String id) {
        return this.resources.containsKey(id);
    }

    // ----------------------------------------
    // CollectionResource
    // ----------------------------------------

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        if (id == null) {
            responder.resourceRead(this);
            return;
        }

        if (!this.resources.containsKey(id)) {
            responder.noSuchResource(id);
            return;
        }

        responder.resourceRead(this.resources.get(id));
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) {
        this.resources.values().forEach((e) -> {
            sink.accept(e);
        });
        sink.close();
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        if (this.resources.containsKey(state.id())) {
            responder.createNotSupported(this);
            return;
        }

        try {
            this.deployer.deploy(state.id(), state, (result) -> {
                if (result.cause() != null) {
                    responder.internalError(result.cause());
                } else {
                    if (this.deploymentManager != null) {
                        try {
                            this.deploymentManager.addConfiguration(state.id(), state);
                        } catch (Exception e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                    responder.resourceCreated(result.rootResource());
                }
            });
        } catch (DeploymentException e) {
            responder.internalError(e);
        }

    }

    @Override
    public Resource parent() {
        return null;
    }

    @Override
    public String id() {
        return this.prefix;
    }

    private String prefix = "";
    private Map<String, RootResource> resources = new ConcurrentHashMap<>();

    private Deployer deployer;
    private DirectoryDeploymentManager deploymentManager;
}


