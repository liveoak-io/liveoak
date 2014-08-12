package io.liveoak.scripts.objects.impl;

import io.liveoak.scripts.objects.Request;
import io.liveoak.scripts.objects.RequestContext;
import io.liveoak.scripts.objects.Resource;
import io.liveoak.scripts.objects.scripting.ScriptingResourceRequest;
import io.liveoak.spi.ResourceRequest;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakResourceRequest implements Request {

    ResourceRequest request;
    Resource resource;
    RequestContext requestContext;

    public LiveOakResourceRequest(ScriptingResourceRequest request) {
        this.request = request;
        if (request.state() != null) {
            resource = new LiveOakResource(request.state());
        }
        if (request.requestContext() != null) {
            requestContext = new LiveOakRequestContext(request.scriptingRequestContext());
        }
    }

    @Override
    public String getId() {
        return request.requestId().toString();
    }

    @Override
    public String getPath() {
        return request.resourcePath().toString();
    }

    @Override
    public String getType() {
        return request.requestType().toString().toLowerCase();
    }

    @Override
    public Resource getResource() {
        return this.resource;
    }

    @Override
    public RequestContext getContext() {
        return this.requestContext;
    }
}
