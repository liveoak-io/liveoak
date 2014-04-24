package io.liveoak.keycloak.server;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@Provider
@PreMatching
public class KeycloakAdminCorsPreflightFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        URI requestUri = req.getUriInfo().getRequestUri();
        if (!requestUri.getPath().startsWith("/auth/rest/admin")) {
            return;
        }

        String origin = req.getHeaderString("Origin");
        if (KeycloakAdminCorsFilter.isValidOrigin(origin, requestUri) && req.getMethod().equals(HttpMethod.OPTIONS)) {
            Response.ResponseBuilder response = Response.ok();

            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Max-Age", "86400");

            if (req.getMethod().equals(HttpMethod.OPTIONS)) {
                String requestMethod = req.getHeaderString("Access-Control-Request-Method");
                if (requestMethod != null) {
                    response.header("Access-Control-Allow-Methods", requestMethod);
                }

                String requestHeaders = req.getHeaderString("Access-Control-Request-Headers");
                if (requestHeaders != null) {
                    response.header("Access-Control-Allow-Headers", requestHeaders);
                }

                req.abortWith(response.build());
            }
        }
    }

}
