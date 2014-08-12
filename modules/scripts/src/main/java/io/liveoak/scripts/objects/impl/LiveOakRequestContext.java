package io.liveoak.scripts.objects.impl;

import java.util.Map;

import io.liveoak.scripts.objects.RequestContext;
import io.liveoak.scripts.objects.SecurityContext;
import io.liveoak.scripts.objects.scripting.ScriptingRequestContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakRequestContext implements RequestContext {

    LiveOakSecurityContext securityContext;
    LiveOakRequestAttributes liveOakRequestAttributes;
    LiveOakResourceParameters liveOakResourceParameters;

    ScriptingRequestContext requestContext;

    public LiveOakRequestContext(ScriptingRequestContext requestContext) {
        this.requestContext = requestContext;

        this.securityContext = new LiveOakSecurityContext(requestContext.securityContext());

        liveOakRequestAttributes = new LiveOakRequestAttributes(requestContext.requestAttributes());

        liveOakResourceParameters = new LiveOakResourceParameters(requestContext);
    }

    @Override
    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return liveOakRequestAttributes;
    }


    @Override
    public Map<String, Object> getParameters() {
        return liveOakResourceParameters;
    }
}
