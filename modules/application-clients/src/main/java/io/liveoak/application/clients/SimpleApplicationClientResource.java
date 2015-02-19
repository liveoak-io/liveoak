package io.liveoak.application.clients;

import java.util.List;
import java.util.stream.Collectors;

import io.liveoak.spi.Application;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
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

        String realm = securityContext != null ? securityContext.getRealm() : null;
        if (realm == null) {
            realm = LiveOak.LIVEOAK_APP_REALM;
        }

        String appKey = (String) state.getProperty("app-key");
        if (appKey != null && appKey.length() > 0) {
            this.appKey = appKey;

            // Clear out and get data from Keycloak fresh, as it is source of truth
            this.redirectUris.clear();
            this.webOrigins.clear();
            this.applicationRoles.clear();

            ApplicationRepresentation app = this.parent.securityClient().application(securityContext.getToken(), realm, appKey);
            if (app != null) {
                this.redirectUris = app.getRedirectUris();
                this.webOrigins = app.getWebOrigins();

                MappingsRepresentation mapRep = this.parent.securityClient().clientScopeMappings(securityContext.getToken(), realm, appKey);
                mapRep.getApplicationMappings().values().stream()
                        .filter(key -> key.equals(this.application.id()))
                        .forEach(appMap -> appMap.getMappings().forEach(roleRep -> this.applicationRoles.add(roleRep.getName())));
            }
        } else {
            // Bootstrap Keycloak with information
            validateId();
            constructAppKey();
            state.putProperty("app-key", this.appKey);

            // Get access token
            String token = this.parent.directAccessClient().accessToken();

            // Create client application in Keycloak
            this.parent.securityClient().createApplication(token, realm, this.appKey);

            // Update Keycloak application with: non full scope, redirect uris and web origins
            ApplicationRepresentation app = this.parent.securityClient().application(token, realm, this.appKey);
            app.setFullScopeAllowed(false);
            app.setRedirectUris(this.redirectUris);
            app.setWebOrigins(this.webOrigins);
            this.parent.securityClient().updateApplication(token, realm, app);

            // Create scope mappings in Keycloak
            checkApplicationRoles(token, realm, this.application.id());
            updateScopeMappings(token, realm, this.application.id());
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
        ApplicationRepresentation app = this.parent.securityClient().application(ctx.securityContext().getToken(), ctx.securityContext().getRealm(), this.appKey);
        app.setRedirectUris(redirectUris);
        app.setWebOrigins(this.webOrigins);
        this.parent.securityClient().updateApplication(ctx.securityContext().getToken(), ctx.securityContext().getRealm(), app);

        this.applicationRoles = state.getPropertyAsList("app-roles");

        // Check keycloak roles
        checkApplicationRoles(ctx.securityContext().getToken(), ctx.securityContext().getRealm(), this.appKey);

        // Update keycloak scope mappings
        updateScopeMappings(ctx.securityContext().getToken(), ctx.securityContext().getRealm(), this.appKey);

        responder.resourceUpdated(this);
    }

    private void checkApplicationRoles(String token, String realm, String appName) throws Exception {
        List<RoleRepresentation> appRoles = this.parent.securityClient().applicationRoles(token, realm, appName);

        if (this.applicationRoles != null) {
            for (String role : this.applicationRoles) {
                if (!appRoles.contains(role)) {
                    // Create role in Keycloak
                    this.parent.securityClient().createApplicationRole(token, realm, appName, role);
                }
            }
        }
    }

    private void updateScopeMappings(String token, String realm, String appName) throws Exception {
        List<RoleRepresentation> rolesList = null;

        if (this.applicationRoles != null) {
            rolesList = this.applicationRoles.stream()
                    .map(role -> new RoleRepresentation(role, null))
                    .collect(Collectors.toList());
        }

        this.parent.securityClient().addClientScopeRolesForApplication(token, realm, this.appKey, appName, rolesList);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.parent.deleteMember(ctx, this);
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
