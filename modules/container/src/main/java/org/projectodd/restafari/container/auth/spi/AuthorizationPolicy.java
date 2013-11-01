package org.projectodd.restafari.container.auth.spi;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface AuthorizationPolicy {

    /**
     * Decide if request is authorized, not authorized or if we don't know
     *
     * @param authRequestContext encapsulates all info about current request, token etc
     * @return true if request is authorized
     */
    AuthorizationDecision isAuthorized(AuthorizationRequestContext authRequestContext);
}
