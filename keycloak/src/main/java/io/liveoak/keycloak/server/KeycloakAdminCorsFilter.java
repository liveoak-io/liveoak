package io.liveoak.keycloak.server;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakAdminCorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (req instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;

            String origin = request.getHeader("Origin");
            if (origin != null) {
                String requestURI = request.getRequestURI();
                if (requestURI.startsWith(origin.replace(":8080", ":8383"))) {
                    response.addHeader("Access-Control-Allow-Origin", origin);
                    response.addHeader("Access-Control-Allow-Credentials", "true");
                    response.addHeader("Access-Control-Max-Age", "86400");

                    if (request.getMethod().equals("OPTIONS")) {
                        String requestMethod = request.getHeader("Access-Control-Request-Method");
                        if (requestMethod != null) {
                            response.addHeader("Access-Control-Allow-Methods", requestMethod);
                        }

                        String requestHeaders = request.getHeader("Access-Control-Request-Headers");
                        if (requestHeaders != null) {
                            response.addHeader("Access-Control-Allow-Headers", requestMethod);
                        }
                    }
                }
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
    }

}
