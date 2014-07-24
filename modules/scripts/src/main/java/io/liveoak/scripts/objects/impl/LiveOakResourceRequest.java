package io.liveoak.scripts.objects.impl;

import io.liveoak.scripts.objects.Request;
import io.liveoak.scripts.objects.RequestContext;
import io.liveoak.scripts.objects.Resource;
import io.liveoak.spi.ResourceRequest;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakResourceRequest implements Request {

    ResourceRequest request;
    Resource resource;
    RequestContext requestContext;

    public LiveOakResourceRequest(ResourceRequest request) {
        this.request = request;
        if (request.state() != null) {
            resource = new LiveOakResource(request.state());
        }
        if (request.requestContext() != null) {
            requestContext = new LiveOakRequestContext(request.requestContext());
        }
    }

    @Override
    public String getRequestID() {
        return request.requestId().toString();
    }

    @Override
    public String getResourcePath() {
        return request.resourcePath().toString();
    }

    @Override
    public void setResourcePath(String resourcePath) {
        //TODO: see if we can modify the resourcePath of the ResourceRequest...
    }

    @Override
    public String getResourceType() {
        return request.requestType().toString().toLowerCase();
    }

    @Override
    public Resource getResource() {
        return this.resource;
    }

    @Override
    public void setResource(Resource resource) {
        //TODO
    }

    @Override
    public RequestContext getRequestContext() {
        return this.requestContext;
    }
}
