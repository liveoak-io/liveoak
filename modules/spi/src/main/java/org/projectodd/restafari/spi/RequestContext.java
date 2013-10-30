package org.projectodd.restafari.spi;

import java.security.Principal;

public abstract class RequestContext {

    protected static ThreadLocal<RequestContext> ctl = new ThreadLocal<>();

    public static RequestContext instance() {
        return ctl.get();
    }

    public abstract Application getApplication();
    public abstract Principal getPrinicpal();
    public abstract Pagination getPagination();
    public abstract ResourceParams getResourceParams();
    public abstract ReturnFields getReturnFields();
}
