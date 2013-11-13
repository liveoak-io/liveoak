package io.liveoak.security.spi;

/**
 * Service providing authorization
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface AuthorizationService {

    /**
     * Decide if request is authorized or not
     *
     * @param authRequestContext encapsulates all info about current request, token etc
     * @return true if request is authorized
     */
    boolean isAuthorized(AuthorizationRequestContext authRequestContext);
}
