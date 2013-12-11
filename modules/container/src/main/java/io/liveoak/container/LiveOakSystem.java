package io.liveoak.container;

import io.liveoak.container.codec.ResourceCodec;
import io.liveoak.container.codec.ResourceCodecManager;
import io.liveoak.container.deploy.DirectDeployer;
import io.liveoak.spi.Container;
import io.liveoak.spi.container.Deployer;
import io.liveoak.spi.container.DirectConnector;
import io.liveoak.spi.container.Server;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class LiveOakSystem {

    public LiveOakSystem(ServiceContainer serviceContainer) {
        this.serviceContainer = serviceContainer;
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

    public DirectConnector directConnector() {
        return (DirectConnector) this.serviceContainer.getService(LiveOak.DIRECT_CONNECTOR).getValue();
    }

    public Server server(String name) {
        return (Server) this.serviceContainer.getService(LiveOak.server(name)).getValue();
    }

    public ResourceCodecManager codecManager() {
        return (ResourceCodecManager) this.serviceContainer.getService( LiveOak.CODEC_MANAGER ).getValue();
    }

    public Deployer deployer() {
        return (Deployer) this.serviceContainer.getService( LiveOak.DEPLOYER ).getValue();
    }

    public DirectDeployer directDeployer() {
        return (DirectDeployer) this.serviceContainer.getService( LiveOak.DIRECT_DEPLOYER ).getValue();
    }

    public Vertx vertx() {
        return (Vertx) this.serviceContainer.getService( LiveOak.VERTX ).getValue();
    }

    private ServiceContainer serviceContainer;

}
