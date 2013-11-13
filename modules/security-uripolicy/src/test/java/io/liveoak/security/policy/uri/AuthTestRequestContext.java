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
    public Application getApplication() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SecurityContext getSecurityContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Pagination getPagination() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ResourcePath getResourcePath() {
        return resourcePath;
    }

    @Override
    public ResourceParams getResourceParams() {
        return resourceParams;
    }

    @Override
    public RequestAttributes getRequestAttributes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RequestType getRequestType() {
        return reqType;
    }

    @Override
    public ReturnFields getReturnFields() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
