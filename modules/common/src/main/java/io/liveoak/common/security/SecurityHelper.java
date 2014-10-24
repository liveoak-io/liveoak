package io.liveoak.common.security;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.client.ClientResourceResponse;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Ken Finnigan
 */
public final class SecurityHelper {
    private SecurityHelper() {
    }

    public static void auth(Client client, DefaultSecurityContext securityContext, String applicationId, String token, Runnable success,
                            Runnable noSuchResource, Consumer<Object> notAuthorized, Consumer<Throwable> handleError) {

        final RequestContext tokenRequestContext = new RequestContext.Builder().build();

        try {
            client.read(tokenRequestContext, applicationId + "/auth/token-info/" + token, resourceResponse -> {
                try {
                    ResourceState state = resourceResponse.state();

                    if (resourceResponse.responseType().equals(ClientResourceResponse.ResponseType.NO_SUCH_RESOURCE)) {
                        if (noSuchResource != null) {
                            noSuchResource.run();
                            return;
                        }
                    } else if (state.getProperty("error") != null) {
                        if (notAuthorized != null) {
                            notAuthorized.accept(state.getProperty("error"));
                            return;
                        }
                    } else {
                        securityContext.setOriginal(token);

                        securityContext.setRealm((String) state.getProperty("realm"));
                        securityContext.setSubject((String) state.getProperty("subject"));
                        securityContext.setLastVerified(((Date) state.getProperty("issued-at")).getTime());
                        securityContext.setUser(
                                new DefaultUserProfile()
                                        .name(state.getPropertyAsString("name"))
                                        .givenName(state.getPropertyAsString("given-name"))
                                        .familyName(state.getPropertyAsString("family-name"))
                                        .email(state.getPropertyAsString("email"))
                        );

                        Set<String> roles = new HashSet<>();
                        roles.addAll((Collection<? extends String>) state.getProperty("roles"));
                        securityContext.setRoles(roles);

                        if (success != null) {
                            success.run();
                        }
                    }
                } catch (Throwable t) {
                    if (handleError != null) {
                        handleError.accept(t);
                    }
                }
            });
        } catch (Throwable t) {
            if (handleError != null) {
                handleError.accept(t);
            }
        }
    }
}
