package io.liveoak.interceptor.service;

import java.util.ArrayList;
import java.util.List;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.interceptor.InterceptorManager;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InterceptorSystemResource implements RootResource {

    private final String id;
    private final InterceptorManager interceptorManager;
    private Resource parent;

    public InterceptorSystemResource(String id, InterceptorManager interceptorManager) {
        this.id = id;
        this.interceptorManager = interceptorManager;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        ResourceState resourceState = this.interceptorManager.getInterceptorsConfig();

        for (String key : resourceState.getPropertyNames()) {
            List<ResourceState> list = (List<ResourceState>)resourceState.getProperty(key);

            // Convert "ResourceState" to "Resource" as this is expected by encoders
            List<Resource> result = new ArrayList<>();
            for (ResourceState interceptorConfig : list) {
                 result.add(new SynchronousResource() {

                     @Override
                     public Resource parent() {
                         return InterceptorSystemResource.this;
                     }

                     @Override
                     public String id() {
                         return null;
                     }

                     @Override
                     public ResourceState properties() throws Exception {
                         return interceptorConfig;
                     }
                 });
            }
            sink.accept(key, result);
        }
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        this.interceptorManager.setInterceptorsConfig(state);
        responder.resourceUpdated(this);
    }
}
