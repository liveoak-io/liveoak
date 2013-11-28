/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

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
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class DefaultRequestContext implements RequestContext {


    private Application app;
    private Pagination pagination;
    private ReturnFields returnFields;
    private ResourceParams resourceParams;
    private SecurityContext securityContext;
    private ResourcePath resourcePath;
    private RequestType requestType;
    private RequestAttributes requestAttributes;
    private Sorting sorting;

    public DefaultRequestContext(SecurityContext securityContext, Pagination pagination, ReturnFields returnFields, ResourceParams resourceParams,
                                 ResourcePath resourcePath, RequestType requestType, RequestAttributes requestAttributes, Sorting sorting) {
        this.securityContext = securityContext;
        this.pagination = pagination;
        this.returnFields = returnFields;
        this.resourceParams = resourceParams;
        this.resourcePath = resourcePath;
        this.requestType = requestType;
        this.requestAttributes = requestAttributes;
        this.sorting = sorting;
    }

    @Override
    public Application application() {
        return app;
    }

    void setApplication(Application app) {
        this.app = app;
    }

    @Override
    public SecurityContext securityContext() {
        return securityContext;
    }

    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    @Override
    public Pagination pagination() {
        return pagination;
    }

    void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    @Override
    public ReturnFields returnFields() {
        return returnFields;
    }

    void setReturnFields(ReturnFields returnFields) {
        this.returnFields = returnFields;
    }

    @Override
    public ResourceParams resourceParams() {
        return resourceParams;
    }

    void setResourceParams(ResourceParams resourceParams) {
        this.resourceParams = resourceParams;
    }

    @Override
    public ResourcePath resourcePath() {
        return resourcePath;
    }

    @Override
    public RequestType requestType() {
        return requestType;
    }

    @Override
    public RequestAttributes requestAttributes() {
        return requestAttributes;
    }

    @Override
    public Sorting sorting() {
        return sorting;
    }
}
