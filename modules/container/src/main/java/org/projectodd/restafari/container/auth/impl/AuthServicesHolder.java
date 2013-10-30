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

    // TODO: Drop this
    private static final String DEFAULT_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCrVrCuTtArbgaZzL1hvh0xtL5mc7o0NqPVnYXkLvgcwiC3BjLGw1tGEGoJaXDuSaRllobm53JBhjx33UNv+5z/UMG4kytBWxheNVKnL6GgqlNabMaFfPLPCF8kAgKnsi79NMo+n6KnSY8YeUmec/p2vjO2NjsSAVcWEQMVhJ31LwIDAQAB";

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
        this.applicationIdResolver = (resourceReq) -> ApplicationIdResolver.DEFAULT_APP_ID;

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
        ApplicationMetadata appMetadata = new ApplicationMetadata(ApplicationIdResolver.DEFAULT_APP_ID, "authTest",
                "authTest", DEFAULT_PUBLIC_KEY);
        authPersister.registerApplicationMetadata(appMetadata);
    }

    private void registerDefaultPolicy() {
        AuthorizationPolicy policy = new DemoAuthorizationPolicy();
        AuthorizationPolicyEntry policyEntry = new AuthorizationPolicyEntry("someId", policy);
        policyEntry.addIncludedResourcePrefix(new ResourcePath());
        authPersister.registerPolicy(ApplicationIdResolver.DEFAULT_APP_ID, policyEntry);
    }
}
