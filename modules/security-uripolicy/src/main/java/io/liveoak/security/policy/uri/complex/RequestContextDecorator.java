/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri.complex;

import io.liveoak.spi.Application;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.SecurityContext;

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
