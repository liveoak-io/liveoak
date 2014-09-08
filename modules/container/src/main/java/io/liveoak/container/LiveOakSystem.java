package io.liveoak.container;

import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.container.extension.ExtensionInstaller;
import io.liveoak.container.resource.PropertiesResource;
import io.liveoak.container.resource.ServersResource;
import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.spi.Services;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.container.Server;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceName;
import org.vertx.java.core.Vertx;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Bob McWhirter
 */
public class LiveOakSystem implements Resource {

    public LiveOakSystem(ServiceContainer serviceContainer) {
        this.serviceContainer = serviceContainer;
        this.propertiesResource = new PropertiesResource(this);
        this.serversResource = new ServersResource(this, serviceContainer);
    }

    public void stop() {
        this.serviceContainer.shutdown();
        try {
            this.serviceContainer.awaitTermination();
        } catch (InterruptedException e) {
            log.error("Interrupted before all services shutdown.", e);
        }
    }

    public Client client() throws InterruptedException {
        return (Client) this.serviceContainer.getService(Services.CLIENT).awaitValue();
    }

    public Server networkServer(String name) throws InterruptedException {
        return (Server) this.serviceContainer.getService(Services.server(name, true)).awaitValue();
    }

    public Server localServer(String name) throws InterruptedException {
        return (Server) this.serviceContainer.getService(Services.server(name, false)).awaitValue();
    }

    public ResourceCodecManager codecManager() throws InterruptedException {
        return (ResourceCodecManager) this.serviceContainer.getService(Services.CODEC_MANAGER).awaitValue();
    }

    public Vertx vertx() throws InterruptedException {
        return (Vertx) this.serviceContainer.getService(Services.VERTX).awaitValue();
    }

    public InternalApplicationRegistry applicationRegistry() throws InterruptedException {
        return (InternalApplicationRegistry) this.serviceContainer.getService(Services.APPLICATION_REGISTRY).awaitValue();
    }

    public ExtensionInstaller extensionInstaller() throws InterruptedException {
        return (ExtensionInstaller) this.serviceContainer.getService(Services.EXTENSION_INSTALLER).awaitValue();
    }

    public Object service(ServiceName name) throws InterruptedException, TimeoutException {
        return this.serviceContainer.getService(name).awaitValue(5, TimeUnit.SECONDS);
    }

    public void awaitStability() throws InterruptedException {
        this.serviceContainer.awaitStability();
    }

    // ----------------------------------------
    // ----------------------------------------

    @Override
    public Resource parent() {
        return null;
    }

    @Override
    public String id() {
        return "system";
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        sink.accept(this.serversResource);
        sink.accept(this.propertiesResource);
        sink.close();
    }

    private ServiceContainer serviceContainer;

    private PropertiesResource propertiesResource;
    private ServersResource serversResource;

    private static final Logger log = Logger.getLogger(LiveOakSystem.class);

}
