package io.liveoak.scripts.objects.impl;

import java.util.List;
import java.util.Map;

import io.liveoak.scripts.objects.Application;
import io.liveoak.scripts.objects.Pagination;
import io.liveoak.scripts.objects.RequestContext;
import io.liveoak.scripts.objects.SecurityContext;
import io.liveoak.scripts.objects.Sorting;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakRequestContext implements RequestContext {

    LiveOakSecurityContext securityContext;
    LiveOakApplication liveOakApplication;
    LiveOakPagination liveOakPagination;
    LiveOakRequestAttributes liveOakRequestAttributes;
    LiveOakResourceParameters liveOakResourceParameters;
    LiveOakSorting liveOakSorting;

    io.liveoak.spi.RequestContext requestContext;

    public LiveOakRequestContext(io.liveoak.spi.RequestContext requestContext) {
        this.requestContext = requestContext;

        this.securityContext = new LiveOakSecurityContext(requestContext.securityContext());

        liveOakApplication = new LiveOakApplication(requestContext.application());

        liveOakPagination = new LiveOakPagination(requestContext.pagination());

        liveOakRequestAttributes = new LiveOakRequestAttributes(requestContext.requestAttributes());

        liveOakResourceParameters = new LiveOakResourceParameters(requestContext.resourceParams());

        if (requestContext.sorting() != null) {
            liveOakSorting = new LiveOakSorting(requestContext.sorting());
        } else {
            liveOakSorting = null;
        }
    }

    @Override
    public Application getApplication() {
        return liveOakApplication;
    }

    @Override
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    @Override
    public Pagination getPagination() {
        return liveOakPagination;
    }

    public Map<String, Object> getRequestAttributes() {
        return liveOakRequestAttributes;
    }

    @Override
    public List<Sorting.Spec> getSorting() {
        return liveOakSorting;
    }

    public Map<String, Object> getResourceParameters() {
        return liveOakResourceParameters;
    }
}
