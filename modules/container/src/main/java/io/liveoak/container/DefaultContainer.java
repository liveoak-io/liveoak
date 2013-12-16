/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.container.codec.ResourceCodec;
import io.liveoak.container.codec.ResourceCodecManager;
import io.liveoak.container.codec.html.HTMLEncoder;
import io.liveoak.container.codec.json.JSONDecoder;
import io.liveoak.container.codec.json.JSONEncoder;
import io.liveoak.container.deploy.ClasspathDeployer;
import io.liveoak.container.deploy.Deployer;
import io.liveoak.container.deploy.JBossModulesDeployer;
import io.liveoak.container.resource.ContainerConfigurationResource;
import io.liveoak.container.subscriptions.SubscriptionManager;
import io.liveoak.spi.Container;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.Configurable;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.PlatformLocator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * Default resource-container implementation.
 *
 * @author Bob McWhirter
 */
@Configurable
public class DefaultContainer implements Container, Resource {

    /**
     * Construct a self-contained container.
     *
     * The container will instantiate its own Vertx platform.
     */
    public DefaultContainer() {
        this(PlatformLocator.factory.createPlatformManager().vertx());
    }


    /**
     * Construct a container around an existing vertx.
     *
     * @param vertx
     */
    public DefaultContainer(Vertx vertx) {
        this.codecManager.registerResourceCodec("application/json", new ResourceCodec(this, JSONEncoder.class, new JSONDecoder()));
        this.codecManager.registerResourceCodec("text/html", new ResourceCodec(this, HTMLEncoder.class, null));

        this.vertx = vertx;
        this.workerPool = Executors.newCachedThreadPool();

        this.subscriptionManager = new SubscriptionManager("subscriptions", this.codecManager);
        try {
            registerResource(this.subscriptionManager, new DefaultResourceState());
        } catch (InitializationException e) {
            // ignore
        }

        this.deployers.put("classpath", new ClasspathDeployer(this));
        this.deployers.put("jboss-module", new JBossModulesDeployer(this));
    }

    @Override
    public void shutdown() {
        this.resources.values().forEach((res) -> {
            res.destroy();
        });

        this.resources.clear();
    }

    public void registerResource(RootResource resource, ResourceState config) throws InitializationException {
        //TODO: Lazy initialization in holder class when resourceRead controller is first accessed
        resource.initialize(new SimpleResourceContext(resource.id(), this.vertx, this));
        Resource configResource = resource.configuration();
        if (configResource != null) {
            RequestContext requestContext = new RequestContext.Builder().build();
            try {
                configResource.updateProperties(requestContext, config, new RegistrationResponder( resource ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.resources.put(resource.id(), resource);
        }
    }

    public void unregisterResource(RootResource resource) {
        this.resources.remove(resource.id());
        resource.destroy();
    }

    public ResourceCodecManager getCodecManager() {
        return this.codecManager;
    }

    public Vertx vertx() {
        return this.vertx;
    }

    SubscriptionManager getSubscriptionManager() {
        return this.subscriptionManager;
    }

    Executor workerPool() {
        return this.workerPool;
    }

    public DirectConnector directConnector() {
        return new DirectConnector(this);
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

        String type = (String) state.getProperty("type");

        Deployer deployer = this.deployers.get(type);

        if (deployer == null) {
            responder.internalError("unknown type: " + type);
            return;
        }

        System.err.println("deploy: " + type + " // " + state);
        RootResource deployed = deployer.deploy(state);
        responder.resourceCreated(deployed);


    }

    @Override
    public Resource parent() {
        return null;
    }

    @Override
    public String id() {
        return this.prefix;
    }

    @Override
    public Resource configuration() {
        return this.configuration;
    }

    private String prefix = "";
    private Map<String, RootResource> resources = new HashMap<>();
    private ResourceCodecManager codecManager = new ResourceCodecManager();
    private Vertx vertx;
    private SubscriptionManager subscriptionManager;
    private Executor workerPool;
    private Map<String, Deployer> deployers = new HashMap<>();

    private Resource configuration = new ContainerConfigurationResource(this);

    // ----------------------------------------------------------------------

    private class RegistrationResponder implements Responder {

        private final RootResource rootResource;

        public RegistrationResponder(RootResource rootResource) {
            this.rootResource = rootResource;
        }

        public void resourceRead(Resource resource) {
        }

        public void resourceCreated(Resource resource) {
        }

        public void resourceDeleted(Resource resource) {
        }

        @Override
        public void resourceUpdated(Resource resource) {
            DefaultContainer.this.resources.put(this.rootResource.id(), (RootResource) this.rootResource);
        }

        @Override
        public void createNotSupported(Resource resource) {
        }

        @Override
        public void readNotSupported(Resource resource) {
        }

        @Override
        public void updateNotSupported(Resource resource) {
            // TODO complain loudly since it's not configurable
        }

        @Override
        public void deleteNotSupported(Resource resource) {
        }

        @Override
        public void noSuchResource(String id) {
        }

        @Override
        public void resourceAlreadyExists(String id) {
        }

        @Override
        public void internalError(String message) {
            // TODO complain loudly
        }

        @Override
        public void internalError(Throwable cause) {
            // TODO complain loudly
        }

        @Override
        public void invalidRequest( String message ) {
            // TODO: complain back to the client
        }

        @Override
        public void invalidRequest( Throwable cause ) {
            // TODO: complain back to the client
        }

        @Override
        public void invalidRequest( String message, Throwable cause ) {
            // TODO: complain back to the client
        }
    }

}


