/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.drools.impl;

import io.liveoak.spi.Application;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.SecurityContext;
import io.liveoak.spi.Sorting;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RequestContextDecorator implements RequestContext {

    private final RequestContext delegate;

    public RequestContextDecorator(RequestContext delegate) {
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
    public ResourceParamsDecorator resourceParams() {
        return new ResourceParamsDecorator(delegate.resourceParams());
    }

    // Needs to be added because Drools are looking for method resourceParams() declared on superclass, which returns just ResourceParams type
    public ResourceParamsDecorator getResourceParams() {
        return resourceParams();
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
}
