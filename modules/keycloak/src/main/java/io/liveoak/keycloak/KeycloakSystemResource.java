package io.liveoak.keycloak;

import io.liveoak.keycloak.service.KeycloakServerService;
import io.liveoak.keycloak.service.KeycloakSessionFactoryService;
import io.liveoak.keycloak.service.UndertowServerService;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.extension.ServiceRestarter;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;

import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 */
public class KeycloakSystemResource implements RootResource, UndertowConfig {


    public KeycloakSystemResource(ServiceTarget target, String id) {
        this.target = target;
        this.id = id;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    public void start() {
        startUndertow();
        startKeycloak();
    }

    private void startUndertow() {
        UndertowServerService undertow = new UndertowServerService();
        this.undertow = this.target.addService(KeycloakServices.undertow(this.id), undertow)
                .addInjection(undertow.configurationInjector(), this)
                .install();
    }

    private void startKeycloak() {
        KeycloakServerService keycloak = new KeycloakServerService();
        this.target.addService(KeycloakServices.keycloak(this.id), keycloak)
                .addDependency(KeycloakServices.undertow(this.id), UndertowServer.class, keycloak.undertowServerInjector())
                .install();

        KeycloakSessionFactoryService sessionFactory = new KeycloakSessionFactoryService();
        this.target.addService(KeycloakServices.sessionFactory(this.id), sessionFactory)
                .addDependency(KeycloakServices.keycloak(this.id), KeycloakServer.class, sessionFactory.keycloakServerInjector())
                .install();
    }

    @Override
    public String host() {
        return this.host;
    }

    @Override
    public int port() {
        return this.port;
    }

    protected synchronized void restart(Consumer<ServiceController<?>> callback) {
        ServiceRestarter.restart(this.undertow, callback);
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        if (state.getProperty("host") != null) {
            this.host = (String) state.getProperty("host");
        }
        if (state.getProperty("port") != null) {
            this.port = (int) state.getProperty("port");
        }
        if (this.undertow != null) {
            restart(new Consumer<ServiceController<?>>() {
                @Override
                public void accept(ServiceController<?> c) {
                    if (c.getState().equals(ServiceController.State.UP)) {
                        responder.resourceUpdated(KeycloakSystemResource.this);
                    } else {
                        responder.internalError(c.getStartException());
                    }
                }
            });
        } else {
            responder.resourceUpdated(this);
        }
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("host", this.host);
        sink.accept("port", this.port);
        sink.close();
    }

    private final ServiceTarget target;
    private final String id;
    private Resource parent;
    private boolean initialized = false;

    private String host = "localhost";
    private int port = 8383;
    private ServiceController<UndertowServer> undertow;
}
