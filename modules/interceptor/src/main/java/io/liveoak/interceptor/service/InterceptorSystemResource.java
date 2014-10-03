package io.liveoak.interceptor.service;

import java.util.List;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.util.ResourceConversionUtils;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.container.interceptor.InterceptorManager;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InterceptorSystemResource implements RootResource, SynchronousResource {

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
    public ResourceState properties(RequestContext ctx) throws Exception {

        ResourceState result = new DefaultResourceState();
        ResourceState resourceState = this.interceptorManager.getInterceptorsConfig();

        for (String key : resourceState.getPropertyNames()) {
            List<ResourceState> resourceStates = (List<ResourceState>) resourceState.getProperty(key);

            // Convert "ResourceState" to "Resource" as this is expected by encoders
            List<Resource> resources = ResourceConversionUtils.convertList(resourceStates, this);
            result.putProperty(key, resources);
        }
        return result;
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        this.interceptorManager.setInterceptorsConfig(state);
        responder.resourceUpdated(this);
    }
}
