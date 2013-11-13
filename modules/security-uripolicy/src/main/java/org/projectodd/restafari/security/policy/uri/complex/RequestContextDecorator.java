package org.projectodd.restafari.security.policy.uri.complex;

import org.projectodd.restafari.spi.Application;
import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.RequestAttributes;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.RequestType;
import org.projectodd.restafari.spi.ResourceParams;
import org.projectodd.restafari.spi.ResourcePath;
import org.projectodd.restafari.spi.ReturnFields;
import org.projectodd.restafari.spi.SecurityContext;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RequestContextDecorator implements RequestContext {

    private final RequestContext delegate;

    public RequestContextDecorator(RequestContext delegate) {
        this.delegate = delegate;
    }

    @Override
    public Application getApplication() {
        return delegate.getApplication();
    }

    @Override
    public SecurityContext getSecurityContext() {
        return delegate.getSecurityContext();
    }

    @Override
    public Pagination getPagination() {
        return delegate.getPagination();
    }

    @Override
    public ResourcePath getResourcePath() {
        return delegate.getResourcePath();
    }

    @Override
    public ResourceParamsDecorator getResourceParams() {
        return new ResourceParamsDecorator(delegate.getResourceParams());
    }

    @Override
    public RequestAttributes getRequestAttributes() {
        return delegate.getRequestAttributes();
    }

    @Override
    public RequestType getRequestType() {
        return delegate.getRequestType();
    }

    @Override
    public ReturnFields getReturnFields() {
        return delegate.getReturnFields();
    }
}
