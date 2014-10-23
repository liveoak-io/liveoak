package io.liveoak.spi;

import java.util.UUID;

import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class DelegatingResourceRequest implements ResourceRequest {

    ResourceRequest delegate;

    public DelegatingResourceRequest(ResourceRequest resourceRequest) {
        delegate = resourceRequest;
    }

    @Override
    public UUID requestId() {
        return delegate.requestId();
    }

    @Override
    public RequestType requestType() {
        return delegate.requestType();
    }

    @Override
    public ResourcePath resourcePath() {
        return delegate.resourcePath();
    }

    @Override
    public ResourceState state() {
        return delegate.state();
    }

    @Override
    public RequestContext requestContext() {
        return delegate.requestContext();
    }

    @Override
    public MediaTypeMatcher mediaTypeMatcher() {
        return delegate.mediaTypeMatcher();
    }
}
