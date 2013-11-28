/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.policy.uri;

import io.liveoak.spi.Application;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.SecurityContext;
import io.liveoak.spi.Sorting;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthTestRequestContext implements RequestContext {

    private final RequestType reqType;
    private final ResourcePath resourcePath;
    private final ResourceParams resourceParams;

    public AuthTestRequestContext(RequestType reqType, ResourcePath resourcePath, ResourceParams resourceParams) {
        this.reqType = reqType;
        this.resourcePath = resourcePath;
        this.resourceParams = resourceParams;
    }

    @Override
    public Application application() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SecurityContext securityContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Pagination pagination() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ResourcePath resourcePath() {
        return resourcePath;
    }

    @Override
    public ResourceParams resourceParams() {
        return resourceParams;
    }

    @Override
    public RequestAttributes requestAttributes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RequestType requestType() {
        return reqType;
    }

    @Override
    public ReturnFields returnFields() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Sorting sorting() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
