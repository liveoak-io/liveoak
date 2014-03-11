package io.liveoak.keycloak;

import io.liveoak.keycloak.service.KeycloakServerService;
import io.liveoak.keycloak.service.KeycloakSessionFactoryService;
import io.liveoak.keycloak.service.UndertowServerService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.Address;
import io.liveoak.spi.extension.ServiceRestarter;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;
import org.keycloak.models.utils.ModelProviderUtils;

import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 */
public class KeycloakSystemResource implements RootResource {

    public static final String MODEL = "model";

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
        setupModelProvider();
        startKeycloak();
    }

    private void startUndertow() {
        UndertowServerService undertow = new UndertowServerService();
        this.undertow = this.target.addService(KeycloakServices.undertow(this.id), undertow)
                .addDependency(LiveOak.ADDRESS, Address.class, undertow.addressInjector())
                .install();
    }

    private void setupModelProvider() {
        System.setProperty(ModelProviderUtils.MODEL_PROVIDER, model);
    }

    private void startKeycloak() {
        KeycloakServerService keycloak = new KeycloakServerService();
        this.keycloak = this.target.addService(KeycloakServices.keycloak(this.id), keycloak)
                .addDependency(KeycloakServices.undertow(this.id), UndertowServer.class, keycloak.undertowServerInjector())
                .install();

        KeycloakSessionFactoryService sessionFactory = new KeycloakSessionFactoryService();
        this.target.addService(KeycloakServices.sessionFactory(this.id), sessionFactory)
                .addDependency(KeycloakServices.keycloak(this.id), KeycloakServer.class, sessionFactory.keycloakServerInjector())
                .install();
    }

    protected synchronized void restart(ServiceController<?> controller, Consumer<ServiceController<?>> callback) {
        ServiceRestarter.restart(controller, callback);
    }

    protected void restart(ServiceController<?> controller, Responder responder) {
        restart(controller, new Consumer<ServiceController<?>>() {
            @Override
            public void accept(ServiceController<?> c) {
                if (c.getState().equals(ServiceController.State.UP)) {
                    responder.resourceUpdated(KeycloakSystemResource.this);
                } else {
                    responder.internalError(c.getStartException());
                }
            }
        });
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        boolean undertowRestartNeeded = false;
        boolean keycloakRestartNeeded = false;

        if (state.getProperty(MODEL) != null) {
            String configModel = (String) state.getProperty(MODEL);
            if (!configModel.equals(this.model)) {
                this.model = configModel;
                setupModelProvider();
                keycloakRestartNeeded = true;
            }
        }

        if (this.undertow != null && undertowRestartNeeded) {
            restart(this.undertow, responder);
        } else if (this.keycloak != null && keycloakRestartNeeded) {
            restart(this.keycloak, responder);
        } else {
            responder.resourceUpdated(this);
        }
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(MODEL, this.model);
        sink.close();
    }

    private final ServiceTarget target;
    private final String id;
    private Resource parent;
    private boolean initialized = false;

    private String model = "mongo";
    private ServiceController<UndertowServer> undertow;
    private ServiceController<KeycloakServer> keycloak;
}
