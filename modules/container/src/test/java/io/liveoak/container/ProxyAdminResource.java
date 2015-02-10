package io.liveoak.container;

import java.util.HashMap;
import java.util.Map;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.extension.ServiceRestarter;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.msc.service.ServiceController;

/**
 * @author Bob McWhirter
 */
public class ProxyAdminResource implements SynchronousResource, RootResource, ProxyConfig {

    private final ServiceController<ProxyConfig> configController;
    private String id;
    private Resource parent;
    private boolean blocking;

    public ProxyAdminResource(String id, ServiceController<ProxyConfig> configController) {
        this.id = id;
        this.configController = configController;
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

    @Override
    public void properties(RequestContext ctx, ResourceState props) throws Exception {
        if ( (boolean) props.getProperty( "blocking" ) ) {
            this.blocking = true;
        } else {
            this.blocking = false;
        }
        new ServiceRestarter( this.configController ).restart();
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, Object> state = new HashMap<>();
        state.put("blocking", this.blocking);
        return state;
    }

    @Override
    public boolean blocking() {
        return this.blocking;
    }
}
