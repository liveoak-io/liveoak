package io.liveoak.scripts.objects.scripting;

import java.util.UUID;

import io.liveoak.spi.*;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptingResourceRequest implements ResourceRequest {

    UUID requestID;
    RequestType requestType;
    ResourcePath resourcePath;
    ResourceState state;
    ScriptingRequestContext requestContext;
    MediaTypeMatcher mediaTypeMatcher;


    public ScriptingResourceRequest(ResourceRequest original) {
        requestID = original.requestId();
        requestType = original.requestType();
        resourcePath = original.resourcePath();
        state = original.state();
        requestContext = new ScriptingRequestContext(original.requestContext());
        mediaTypeMatcher = original.mediaTypeMatcher();
    }

    @Override
    public UUID requestId() {
        return requestID;
    }

    @Override
    public RequestType requestType() {
        return requestType;
    }

    @Override
    public ResourcePath resourcePath() {
        return resourcePath;
    }

    @Override
    public ResourceState state() {
        return state;
    }

    @Override
    public RequestContext requestContext() {
        return requestContext;
    }

    public ScriptingRequestContext scriptingRequestContext() {
        return requestContext;
    }

    @Override
    public MediaTypeMatcher mediaTypeMatcher() {
        return mediaTypeMatcher;
    }
}
