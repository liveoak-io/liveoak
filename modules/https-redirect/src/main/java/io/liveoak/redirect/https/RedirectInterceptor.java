package io.liveoak.redirect.https;

import java.util.List;
import java.util.UUID;

import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;
import io.liveoak.spi.container.interceptor.Interceptor;
import io.liveoak.spi.container.interceptor.OutboundInterceptorContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RedirectInterceptor implements Interceptor {

    private RedirectManager redirectManager;

    public RedirectInterceptor(RedirectManager redirectManager) {
        this.redirectManager = redirectManager;
    }

    @Override
    public void onInbound(InboundInterceptorContext context) throws Exception {
        List<ResourcePath.Segment> resourcePaths = context.request().resourcePath().segments();
        String applicationName = "/";
        if (resourcePaths.size() > 0) {
            applicationName = resourcePaths.get(0).name();
        }

        if (applicationName != "/") {
            ResourceResponse response = redirectManager.generateRedirectResponse(applicationName, context.request());
            if (response != null) {
                context.replyWith(response);
                return;
            }
        }

        //Otherwise, just continue, we don't need to perform an https redirect
        context.forward();

    }

    @Override
    public void onOutbound(OutboundInterceptorContext context) throws Exception {
        // onOutbound does not apply to a redirect
        context.forward();
    }

    @Override
    public void onComplete(UUID requestId) {
        // do nothing for now
    }
}
