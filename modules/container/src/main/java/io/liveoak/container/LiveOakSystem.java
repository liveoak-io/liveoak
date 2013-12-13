package io.liveoak.container;

import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.container.deploy.DirectDeployer;
import io.liveoak.container.resource.PropertiesResource;
import io.liveoak.container.resource.ServersResource;
import io.liveoak.spi.Container;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.container.Deployer;
import io.liveoak.spi.container.Server;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.ResourceSink;
import org.jboss.msc.service.ServiceContainer;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class LiveOakSystem implements RootResource {

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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public Container container() {
        return (Container) this.serviceContainer.getService(LiveOak.CONTAINER).getValue();
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

    public Deployer deployer() {
        return (Deployer) this.serviceContainer.getService(LiveOak.DEPLOYER).getValue();
    }

    public DirectDeployer directDeployer() {
        return (DirectDeployer) this.serviceContainer.getService(LiveOak.DIRECT_DEPLOYER).getValue();
    }

    public Vertx vertx() {
        return (Vertx) this.serviceContainer.getService(LiveOak.VERTX).getValue();
    }

    // ----------------------------------------
    // ----------------------------------------

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
}
