package io.liveoak.container.tenancy;

import io.liveoak.common.DefaultMountPointResource;
import io.liveoak.spi.Application;
import io.liveoak.spi.DelegatingRequestContext;
import io.liveoak.spi.DelegatingResourceRequest;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;

/**
 * @author Bob McWhirter
 */
public class GlobalContext extends DefaultMountPointResource {

    public GlobalContext() {
        super("");
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        Resource value = member(ctx, id);

        if (ctx instanceof ApplicationContextRequestContext && value instanceof ApplicationContext) {
            ApplicationContextRequestContext requestContext = (ApplicationContextRequestContext)ctx;
            Application application = ((ApplicationContext)value).application();
            requestContext.application(application);
        }

        super.readMember(ctx, id, responder);
    }

    private class ApplicationContextRequestContext extends DelegatingRequestContext {

        Application application;

        public ApplicationContextRequestContext(RequestContext delegate) {
            super(delegate);
        }

        @Override
        public Application application() {
            return application;
        }

        private void application(Application application) {
            this.application = application;
        }
    }

    public ResourceRequest generateResourceRequest(ResourceRequest resourceRequest) {
        return new ResourceRequestWrapper(resourceRequest);
    }

    private class ResourceRequestWrapper extends DelegatingResourceRequest {

        private ApplicationContextRequestContext requestContext;

        public ResourceRequestWrapper(ResourceRequest resourceRequest) {
            super(resourceRequest);
            this.requestContext = new ApplicationContextRequestContext(resourceRequest.requestContext());
        }

        @Override
        public RequestContext requestContext() {
            return requestContext;
        }
    }
}
