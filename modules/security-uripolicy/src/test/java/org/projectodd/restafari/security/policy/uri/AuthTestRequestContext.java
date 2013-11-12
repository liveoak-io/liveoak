package org.projectodd.restafari.security.policy.uri;

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
