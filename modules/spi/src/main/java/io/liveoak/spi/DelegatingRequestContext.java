package io.liveoak.spi;

import io.liveoak.spi.security.SecurityContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class DelegatingRequestContext implements RequestContext {

    RequestContext delegate;

    public DelegatingRequestContext(RequestContext delegate) {
        this.delegate = delegate;
    }

    @Override
    public Application application() {
        return delegate.application();
    }

    @Override
    public SecurityContext securityContext() {
        return delegate.securityContext();
    }

    @Override
    public Pagination pagination() {
        return delegate.pagination();
    }

    @Override
    public ResourcePath resourcePath() {
        return delegate.resourcePath();
    }

    @Override
    public ResourceParams resourceParams() {
        return delegate.resourceParams();
    }

    @Override
    public RequestAttributes requestAttributes() {
        return delegate.requestAttributes();
    }

    @Override
    public RequestType requestType() {
        return delegate.requestType();
    }

    @Override
    public ReturnFields returnFields() {
        return delegate.returnFields();
    }

    @Override
    public Sorting sorting() {
        return delegate.sorting();
    }

    @Override
    public void dispose() {
        delegate.dispose();
    }

    @Override
    public void onDispose(Runnable runnable) {
        delegate.onDispose(runnable);
    }
}
