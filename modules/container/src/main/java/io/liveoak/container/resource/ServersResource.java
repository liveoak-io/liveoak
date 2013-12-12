package io.liveoak.container.resource;

import java.util.List;

import io.liveoak.container.LiveOak;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.Server;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class ServersResource implements Resource {

    public ServersResource(Resource parent, ServiceContainer serviceContainer) {
        this.parent = parent;
        this.serviceContainer = serviceContainer;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return "servers";
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        List<ServiceName> names = this.serviceContainer.getServiceNames();
        for ( ServiceName name : names ) {
            if (LiveOak.SERVER.equals( name.getParent() ) ) {
                sink.accept( new ServerResource( this, name.getSimpleName(), (Server) this.serviceContainer.getService( name ).getValue()) );
            }
        }

        sink.close();
    }

    private Resource parent;
    private ServiceContainer serviceContainer;

}
