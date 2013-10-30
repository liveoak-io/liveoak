package org.projectodd.restafari.container;

import org.projectodd.restafari.spi.Application;
import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.ResourceParams;
import org.projectodd.restafari.spi.ReturnFields;

import java.security.Principal;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class DefaultRequestContext extends RequestContext {

    public static void associate(RequestContext ctx) {
        ctl.set(ctx);
    }

    public static void dissociate() {
        ctl.remove();
    }

    public DefaultRequestContext(Principal principal, Pagination pagination, ReturnFields returnFields, ResourceParams resourceParams) {
        this.principal = principal;
        this.pagination = pagination;
        this.returnFields = returnFields;
        this.resourceParams = resourceParams;
    }

    @Override
    public Application getApplication() {
        return app;
    }

    void setApplication(Application app) {
        this.app = app;
    }

    @Override
    public Principal getPrinicpal() {
        return principal;
    }

    void setPrincipal(Principal principal) {
        this.principal = principal;
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

    private Application app;
    private Principal principal;
    private Pagination pagination;
    private ReturnFields returnFields;
    private ResourceParams resourceParams;
}
