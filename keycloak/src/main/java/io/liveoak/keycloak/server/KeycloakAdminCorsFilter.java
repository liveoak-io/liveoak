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
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@Provider
public class KeycloakAdminCorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        URI requestUri = req.getUriInfo().getRequestUri();
        if (!requestUri.getPath().startsWith("/auth/rest/admin")) {
            return;
        }

        String origin = req.getHeaderString("Origin");
        if (isValidOrigin(origin, requestUri) && !req.getMethod().equals(HttpMethod.OPTIONS)) {
            res.getHeaders().add("Access-Control-Allow-Origin", origin);
//            res.getHeaders().add("Access-Control-Allow-Credentials", "true");
        }
    }

    public static boolean isValidOrigin(String origin, URI requestUri) {
        if (origin == null) {
            return false;
        }

        try {
            URI originUri = new URI(origin);
            return originUri.getScheme().equals(requestUri.getScheme()) && originUri.getHost().equals(requestUri.getHost());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

}
