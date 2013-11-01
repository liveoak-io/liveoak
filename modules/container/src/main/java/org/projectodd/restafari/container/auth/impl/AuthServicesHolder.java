package org.projectodd.restafari.container.auth.impl;

import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.container.auth.impl.uri.DemoAuthorizationPolicy;
import org.projectodd.restafari.container.auth.spi.*;

/**
 * Container for various services related to authentication/authorization
 * TODO: Probably remove later...
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthServicesHolder {

    private static AuthServicesHolder INSTANCE = new AuthServicesHolder();

    private final AuthorizationService authorizationService;
    private final AuthPersister authPersister;
    private final TokenManager tokenManager;

    // TODO: Probably remove
    private ApplicationIdResolver applicationIdResolver;

    private AuthServicesHolder() {
        this.authorizationService = new PolicyBasedAuthorizationService();
        this.authPersister = new InMemoryAuthPersister();
        this.tokenManager = new TokenManagerImpl();
        this.applicationIdResolver = (resourceReq) -> AuthConstants.DEFAULT_APP_ID;

        // Register default metadata and URIPolicy for default application
        registerDefaultAppConfig();
        registerDefaultPolicy();
    };

    public static AuthServicesHolder getInstance() {
        return INSTANCE;
    }

    public AuthorizationService getAuthorizationService() {
        return authorizationService;
    }

    public AuthPersister getAuthPersister() {
        return authPersister;
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    public ApplicationIdResolver getApplicationIdResolver() {
        return applicationIdResolver;
    }

    public void setApplicationIdResolver(ApplicationIdResolver applicationIdResolver) {
        this.applicationIdResolver = applicationIdResolver;
    }

    private void registerDefaultAppConfig() {
        ApplicationMetadata appMetadata = new ApplicationMetadata(AuthConstants.DEFAULT_APP_ID, AuthConstants.DEFAULT_REALM_NAME,
                AuthConstants.DEFAULT_APPLICATION_NAME, AuthConstants.DEFAULT_PUBLIC_KEY);
        authPersister.registerApplicationMetadata(appMetadata);
    }

    private void registerDefaultPolicy() {
        AuthorizationPolicy policy = new DemoAuthorizationPolicy();
        AuthorizationPolicyEntry policyEntry = new AuthorizationPolicyEntry("someId", policy);
        policyEntry.addIncludedResourcePrefix(new ResourcePath());
        authPersister.registerPolicy(AuthConstants.DEFAULT_APP_ID, policyEntry);
    }
}
