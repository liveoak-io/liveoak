package io.liveoak.scripts.objects.impl;

import io.liveoak.scripts.objects.Request;
import io.liveoak.scripts.objects.Resource;
import io.liveoak.scripts.objects.Response;
import io.liveoak.scripts.objects.scripting.ScriptingResourceRequest;
import io.liveoak.spi.ResourceResponse;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakResourceResponse implements Response {

    ResourceResponse resourceResponse;
    LiveOakResourceRequest liveOakResourceRequest;
    LiveOakResource liveOakResource;

    public LiveOakResourceResponse(ResourceResponse resourceResponse) {
        this.resourceResponse = resourceResponse;

        liveOakResourceRequest = new LiveOakResourceRequest(new ScriptingResourceRequest(resourceResponse.inReplyTo()));

        liveOakResource = new LiveOakResource(resourceResponse.state());
    }

    @Override
    public String getType() {
        return resourceResponse.responseType().toString().toLowerCase();
    }

    @Override
    public Resource getResource() {
        return liveOakResource;
    }

    @Override
    public Request getRequest() {
        return liveOakResourceRequest;
    }
}
