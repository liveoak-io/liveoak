package io.liveoak.redirect.https;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.common.DefaultResourceMovedResponse;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.DefaultSecurityContext;
import io.liveoak.container.protocols.http.HttpResourceRequestDecoder;
import io.liveoak.redirect.https.resource.ApplicationRedirectConfig;
import io.liveoak.redirect.https.resource.Redirect;
import io.liveoak.redirect.https.resource.SystemRedirectConfig;
import io.liveoak.spi.DelegatingRequestContext;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourceMovedResponse;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.security.SecurityContext;
import io.liveoak.spi.state.ResourceState;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RedirectManager {

    static final Logger log = Logger.getLogger(RedirectManager.class);

    private Map<String, ApplicationRedirectConfig> appRedirects;

    private SystemRedirectConfig systemRedirectConfig;
    private Client client;

    public RedirectManager(SystemRedirectConfig systemRedirectConfig, Client client) {
        appRedirects = new ConcurrentHashMap<>();
        this.systemRedirectConfig = systemRedirectConfig;
        this.client = client;
    }

    public ResourceResponse generateRedirectResponse(String applicationId, ResourceRequest request) {
        try {
        ApplicationRedirectConfig config = appRedirects.get(applicationId);
        // We don't have a configuration for this resource, then return null (signifies no https redirect)
        if (config == null) {
            return null;
        }
        if (performRedirect(applicationId, config, request)) {

                RequestAttributes attributes = request.requestContext().requestAttributes();

                // If are not dealing with a proper HTTP connection or we cannot get the headers, throw an exception
                if (attributes == null || attributes.getAttribute(HttpResourceRequestDecoder.HTTP_REQUEST) == null || !(attributes.getAttribute(HttpResourceRequestDecoder.HTTP_REQUEST) instanceof DefaultHttpRequest)) {
                    return new DefaultResourceErrorResponse(request, ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE, "Not Acceptable: invalid request.");
                }

                HttpHeaders headers = ((DefaultHttpRequest) attributes.getAttribute(HttpResourceRequestDecoder.HTTP_REQUEST)).headers();

                String scheme = headers.get("X-Forwarded-Proto");

                if (scheme == null) {
                    return new DefaultResourceErrorResponse(request, ResourceErrorResponse.ErrorType.NOT_ACCEPTABLE, "Not Acceptable: invalid request.");
                }

                if (scheme.equals("https")) {
                    return null;
                } else {
                    String host = headers.get("Host");
                    String fullPath = "https://" + host + request.resourcePath();
                    URI uri = new URI(fullPath);

                    Redirect.Types type = getRedirectType(config);
                    Integer maxAge = getRedirectMaxAge(config);

                    ResourceMovedResponse.MovedType movedType = ResourceMovedResponse.MovedType.MOVED_TEMPORARILY;
                    if (type == Redirect.Types.PERMANENT) {
                        movedType = ResourceMovedResponse.MovedType.MOVED_PERMANENTLY;
                    }

                    DefaultResourceMovedResponse response = new DefaultResourceMovedResponse(request, movedType, uri);

                    if (maxAge != null) {
                        response.maxAge(maxAge);
                    }
                    return response;
                }

        } else {
            return null;
        }
        } catch (Exception e) {
            log.error("An error occurred while processing the https redirect conditions", e);
            return new DefaultResourceErrorResponse(request, ResourceErrorResponse.ErrorType.INTERNAL_ERROR, "An error occurred while processing the https redirect conditions.");
        }
    }


    protected boolean performRedirect(String applicationId, ApplicationRedirectConfig config, ResourceRequest request) throws Exception {
        if (config != null) {
            ApplicationRedirectConfig.Options option = config.redirect();

            if (option == null) {
                option = systemRedirectConfig.getDefaultRedirect().redirect();
            }

            switch (option){
                case NONE:
                    return false;
                case ALL:
                    return true;
                case SECURED:
                    return checkSecured(applicationId, config, request);
            }

            return true;
        } else {
            return false;
        }
    }

    protected Redirect.Types getRedirectType(ApplicationRedirectConfig config) {

        Redirect.Types type =  config.type();

        if (type == null) {
            type = systemRedirectConfig.getDefaultRedirect().type();
        }

        return type;
    }

    protected Integer getRedirectMaxAge(ApplicationRedirectConfig config) {
        Integer maxAge = config.maxAge();

        if (maxAge == null) {
            maxAge = systemRedirectConfig.getDefaultRedirect().maxAge();
        }

        return maxAge;
    }

    protected boolean checkSecured(String applicationId, ApplicationRedirectConfig config, ResourceRequest request) throws Exception {

        if (request.requestContext().securityContext() != null && request.requestContext().securityContext().isAuthenticated()) {
            return true;
        }

        String authorization = ((DefaultHttpRequest)request.requestContext().requestAttributes().getAttribute(HttpResourceRequestDecoder.HTTP_REQUEST)).headers().get("Authorization");
        if (authorization != null) {
            return true;
        }

        RequestAttributes attribs = new DefaultRequestAttributes();
        attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, new UnAuthorizedRequestContext(request.requestContext()));
        attribs.setAttribute(AuthzConstants.ATTR_REQUEST_RESOURCE_STATE, request.state());
        RequestContext authzRequest = new RequestContext.Builder().requestAttributes(attribs).build();

            ResourceState resourceState = client.read(authzRequest, "/" + applicationId + "/authz/authzCheck");

            boolean authorized = (Boolean) resourceState.getProperty(AuthzConstants.ATTR_AUTHZ_RESULT);

        if (authorized) {
                return false;
            } else {
            return true;
        }
    }

    private class UnAuthorizedRequestContext extends DelegatingRequestContext {
        public UnAuthorizedRequestContext(RequestContext delegate) {
            super(delegate);
        }

        @Override
        public SecurityContext securityContext() {
            return new DefaultSecurityContext();
        }
    }

    public void addRedirect(String applicationId, ApplicationRedirectConfig applicationRedirectConfig) {
        appRedirects.put(applicationId, applicationRedirectConfig);
    }

    public ApplicationRedirectConfig removeRedirect(String applicationId) {
        return appRedirects.remove(applicationId);
    }

}
