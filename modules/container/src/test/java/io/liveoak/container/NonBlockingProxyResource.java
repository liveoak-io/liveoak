package io.liveoak.container;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class NonBlockingProxyResource implements RootResource {

    private String id;
    private Client client;
    private Resource parent;

    public NonBlockingProxyResource(String id, Client client) {
        this.id = id;
        this.client = client;
    }

    @Override
    public String id() {
        return this.id;
    }

    public void parent(Resource parent) {
        this.parent = parent;
    }

    public Resource parent() {
        return this.parent;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        RequestContext requestContext = new RequestContext.Builder().build();
        client.read(requestContext, "/testApp/db/people/proxybob", (r) -> {
            try {
                ResourceState result = r.state();
                for (String n : result.getPropertyNames()) {
                    sink.accept(n, result.getProperty(n));
                }
            } catch (Throwable e) {
                sink.error(e);
            } finally {
                sink.complete();
            }
        });
    }

}
