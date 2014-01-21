package io.liveoak.container;

import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.container.extension.ExtensionInstaller;
import io.liveoak.container.tenancy.InternalOrganizationRegistry;
import io.liveoak.container.tenancy.OrganizationRegistry;
import io.liveoak.container.resource.PropertiesResource;
import io.liveoak.container.resource.ServersResource;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.container.Server;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceName;
import org.vertx.java.core.Vertx;

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

    public Client client() {
        return (Client) this.serviceContainer.getService(LiveOak.CLIENT).getValue();
    }

    public Server networkServer(String name) {
        return (Server) this.serviceContainer.getService(LiveOak.server(name, true)).getValue();
    }

    public Server localServer(String name) {
        return (Server) this.serviceContainer.getService(LiveOak.server(name, false)).getValue();
    }

    public ResourceCodecManager codecManager() {
        return (ResourceCodecManager) this.serviceContainer.getService(LiveOak.CODEC_MANAGER).getValue();
    }

    public Vertx vertx() {
        return (Vertx) this.serviceContainer.getService(LiveOak.VERTX).getValue();
    }

    public InternalOrganizationRegistry organizationRegistry() {
        return (InternalOrganizationRegistry) this.serviceContainer.getService(LiveOak.ORGANIZATION_REGISTRY).getValue();
    }

    public ExtensionInstaller extensionInstaller() {
        return (ExtensionInstaller) this.serviceContainer.getService(LiveOak.EXTENSION_INSTALLER).getValue();
    }

    public Object service(ServiceName name) throws InterruptedException {
        return this.serviceContainer.getService(name).awaitValue();
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
