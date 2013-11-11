package org.projectodd.restafari.container;

import org.projectodd.restafari.spi.Application;
import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.RequestAttributes;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.RequestType;
import org.projectodd.restafari.spi.ResourceParams;
import org.projectodd.restafari.spi.ResourcePath;
import org.projectodd.restafari.spi.ReturnFields;
import org.projectodd.restafari.spi.SecurityContext;

import java.security.Principal;

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

    public DefaultRequestContext(SecurityContext securityContext, Pagination pagination, ReturnFields returnFields, ResourceParams resourceParams,
                                 ResourcePath resourcePath, RequestType requestType, RequestAttributes requestAttributes) {
        this.securityContext = securityContext;
        this.pagination = pagination;
        this.returnFields = returnFields;
        this.resourceParams = resourceParams;
        this.resourcePath = resourcePath;
        this.requestType = requestType;
        this.requestAttributes = requestAttributes;
    }

    @Override
    public Application getApplication() {
        return app;
    }

    void setApplication(Application app) {
        this.app = app;
    }

    @Override
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    public void setSecurityContext(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    @Override
    public Pagination getPagination() {
        return pagination;
    }

    void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    @Override
    public ReturnFields getReturnFields() {
        return returnFields;
    }

    void setReturnFields(ReturnFields returnFields) {
        this.returnFields = returnFields;
    }

    @Override
    public ResourceParams getResourceParams() {
        return resourceParams;
    }

    void setResourceParams(ResourceParams resourceParams) {
        this.resourceParams = resourceParams;
    }

    @Override
    public ResourcePath getResourcePath() {
        return resourcePath;
    }

    @Override
    public RequestType getRequestType() {
        return requestType;
    }

    @Override
    public RequestAttributes getRequestAttributes() {
        return requestAttributes;
    }
}
