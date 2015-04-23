package io.liveoak.application.clients;

import java.util.List;
import java.util.stream.Collectors;

import io.liveoak.spi.Application;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.ResourceProcessingException;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.resource.config.ConfigResource;
import io.liveoak.spi.security.SecurityContext;
import io.liveoak.spi.state.ResourceState;
import org.keycloak.representations.idm.ApplicationRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

/**
 * @author Ken Finnigan
 */
public class SimpleApplicationClientResource implements Resource, ConfigResource {

    public SimpleApplicationClientResource(ApplicationClientsResource parent, Application application, SecurityContext securityContext, ResourceState state) throws Exception {
        this.parent = parent;
        this.application = application;

        this.id = state.id();
        this.type = (String) state.getProperty("type");
        this.redirectUris = state.getPropertyAsList("redirect-uris");
        this.webOrigins = state.getPropertyAsList("web-origins");
        this.applicationRoles = state.getPropertyAsList("app-roles");

        // Get access token
        String token;
        if (securityContext != null && securityContext.getToken() != null) {
            token = securityContext.getToken();
        } else {
            token = this.parent.directAccessClient().accessToken();
        }

        String appKey = (String) state.getProperty("app-key");
        if (appKey != null && appKey.length() > 0) {
            this.appKey = appKey;

            // Clear out and get data from Keycloak fresh, as it is source of truth
            this.redirectUris.clear();
            this.webOrigins.clear();
            this.applicationRoles.clear();

            ApplicationRepresentation app = this.parent.securityClient().application(token, LiveOak.LIVEOAK_APP_REALM, appKey);
            if (app != null) {
                this.redirectUris = app.getRedirectUris();
                this.webOrigins = app.getWebOrigins();

                MappingsRepresentation mapRep = this.parent.securityClient().clientScopeMappings(token, LiveOak.LIVEOAK_APP_REALM, appKey);
                mapRep.getApplicationMappings().values().stream()
                        .filter(value -> value.getApplication().equals(this.application.id()))
                        .forEach(appMap -> appMap.getMappings().forEach(roleRep -> this.applicationRoles.add(roleRep.getName())));
            }
        } else {
            // Bootstrap Keycloak with information
            validateId();
            constructAppKey();
            state.putProperty("app-key", this.appKey);

            // Create client application in Keycloak
            this.parent.securityClient().createApplication(token, LiveOak.LIVEOAK_APP_REALM, this.appKey);

            // Update Keycloak application with: public client, non full scope, redirect uris and web origins
            ApplicationRepresentation app = this.parent.securityClient().application(token, LiveOak.LIVEOAK_APP_REALM, this.appKey);
            app.setPublicClient(true);
            app.setFullScopeAllowed(false);
            app.setRedirectUris(this.redirectUris);
            app.setWebOrigins(this.webOrigins);

            // Create scope mappings in Keycloak
            checkApplicationRoles(token, true);
            updateScopeMappings(token);

            // Set default role to 'user' if present
            if (this.applicationRoles.contains("user")) {
                app.setDefaultRoles(new String[]{"user"});
            }

            this.parent.securityClient().updateApplication(token, LiveOak.LIVEOAK_APP_REALM, app);
        }
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("type", this.type);
        sink.accept("app-key", this.appKey);
        sink.accept("redirect-uris", this.redirectUris);
        sink.accept("web-origins", this.webOrigins);
        sink.accept("app-roles", this.applicationRoles);

        sink.complete();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        state.putProperty("app-key", this.appKey);

        this.type = (String) state.getProperty("type");

        this.redirectUris = state.getPropertyAsList("redirect-uris");
        this.webOrigins = state.getPropertyAsList("web-origins");

        // Update keycloak application
        ApplicationRepresentation app = this.parent.securityClient().application(ctx.securityContext().getToken(), LiveOak.LIVEOAK_APP_REALM, this.appKey);
        app.setRedirectUris(redirectUris);
        app.setWebOrigins(this.webOrigins);
        this.parent.securityClient().updateApplication(ctx.securityContext().getToken(), LiveOak.LIVEOAK_APP_REALM, app);

        this.applicationRoles = state.getPropertyAsList("app-roles");

        // Check keycloak roles
        checkApplicationRoles(ctx.securityContext().getToken(), false);

        // Update keycloak scope mappings
        updateScopeMappings(ctx.securityContext().getToken());

        responder.resourceUpdated(this);
    }

    private void checkApplicationRoles(String token, boolean bootstrap) throws Exception {
        List<RoleRepresentation> appRoles = null;

        try {
            appRoles = this.parent.securityClient().applicationRoles(token, LiveOak.LIVEOAK_APP_REALM, this.application.id());
        } catch (ResourceProcessingException e) {
            // If we're bootstrapping the application client, it probably won't exist in Keycloak so may throw an error from SecurityClient
            if (!bootstrap) throw e;
        }

        if (this.applicationRoles != null) {
            for (String role : this.applicationRoles) {
                boolean roleFound = false;

                if (appRoles != null) {
                    for (RoleRepresentation appRole : appRoles) {
                        if (appRole.getName().equals(role)) {
                            roleFound = true;
                            break;
                        }
                    }
                }

                if (!roleFound) {
                    // Create role in Keycloak
                    this.parent.securityClient().createApplicationRole(token, LiveOak.LIVEOAK_APP_REALM, this.application.id(), role);
                }
            }
        }
    }

    private void updateScopeMappings(String token) throws Exception {
        List<RoleRepresentation> rolesList = null;

        if (this.applicationRoles != null) {
            rolesList = this.applicationRoles.stream()
                    .map(role -> new RoleRepresentation(role, null))
                    .collect(Collectors.toList());
        }

        this.parent.securityClient().addClientScopeRolesForApplication(token, LiveOak.LIVEOAK_APP_REALM, this.appKey, this.application.id(), rolesList);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.parent.securityClient().deleteApplication(ctx.securityContext().getToken(), LiveOak.LIVEOAK_APP_REALM, this.appKey);
        this.parent.deleteMember(this.id);
        responder.resourceDeleted(this);
    }

    private void validateId() {
        long dotCount = this.id().toLowerCase().chars().filter(ch -> ch == '.').count();

        if (dotCount > 0) {
            this.id = this.id().replace("liveoak.", "");
            this.id = this.id().replace("client.", "");
            this.id = this.id().replace(this.application.id() + ".", "");
        }
    }

    private void constructAppKey() {
        this.appKey = this.application.id() + ".client." + this.id();
    }

    private ApplicationClientsResource parent;
    private Application application;

    private String id;
    private String type;
    private String appKey;
    private List<String> redirectUris;
    private List<String> webOrigins;
    private List<String> applicationRoles;
}
