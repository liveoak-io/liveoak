package io.liveoak.container.resource;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import io.liveoak.container.server.LocalServer;
import io.liveoak.spi.Services;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.NetworkServer;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class ServersResource implements SynchronousResource {

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
    public Collection<Resource> members(RequestContext ctx) throws Exception {
        LinkedList<Resource> members = new LinkedList<>();
        List<ServiceName> names = this.serviceContainer.getServiceNames();
        for (ServiceName name : names) {
            if (Services.NETWORK_SERVER.equals(name.getParent())) {
                members.add(new NetworkServerResource(this, name.getSimpleName(), (NetworkServer) this.serviceContainer.getService(name).getValue()));
            } else if (Services.LOCAL_SERVER.equals(name.getParent())) {
                members.add(new LocalServerResource(this, name.getSimpleName(), (LocalServer) this.serviceContainer.getService(name).getValue()));
            }
        }
        return members;
    }

    private Resource parent;
    private ServiceContainer serviceContainer;

}
