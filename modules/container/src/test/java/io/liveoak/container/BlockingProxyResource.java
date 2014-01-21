package io.liveoak.container;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.BlockingResource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class BlockingProxyResource implements RootResource, BlockingResource {
    private final String id;
    private Client client;
    private Resource parent;

    public BlockingProxyResource(String id, Client client) {
        this.id = id;
        this.client = client;
    }

    @Override
    public String id() {
        return this.id;
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
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        RequestContext requestContext = new RequestContext.Builder().build();
        ResourceState result = client.read(requestContext, "/testOrg/testApp/db/people/blockingproxybob");
        for (String n : result.getPropertyNames()) {
            sink.accept(n, result.getProperty(n));
        }
        try {
            sink.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
