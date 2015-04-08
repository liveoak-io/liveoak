package io.liveoak.application.clients;

import java.util.ArrayList;
import java.util.List;

import io.liveoak.security.client.SecurityClient;
import org.keycloak.representations.idm.ApplicationRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

/**
 * @author Ken Finnigan
 */
public class MockSecurityClient extends SecurityClient {
    public MockSecurityClient() {
        super("");
    }

    @Override
    public List<ApplicationRepresentation> applications(String authToken, String realmName) throws Exception {
        return null;
    }

    @Override
    public ApplicationRepresentation application(String authToken, String realmName, String appName) throws Exception {
        return new ApplicationRepresentation();
    }

    @Override
    public void updateApplication(String authToken, String realmName, ApplicationRepresentation app) throws Exception {
    }

    @Override
    public void createApplication(String authToken, String realmName, String appName) throws Exception {
    }

    @Override
    public void deleteApplication(String authToken, String realmName, String appName) throws Exception {
    }

    @Override
    public List<RoleRepresentation> applicationRoles(String authToken, String realmName, String appName) throws Exception {
        return new ArrayList<>();
    }

    @Override
    public void createApplicationRole(String authToken, String realmName, String appName, String roleName) throws Exception {
    }

    @Override
    public void deleteApplicationRole(String authToken, String realmName, String appName, String roleName) throws Exception {
    }

    @Override
    public MappingsRepresentation clientScopeMappings(String authToken, String realmName, String clientApp) throws Exception {
        return null;
    }

    @Override
    public List<RoleRepresentation> clientScopeRolesForApplication(String authToken, String realmName, String clientApp, String appName) throws Exception {
        return null;
    }

    @Override
    public void addClientScopeRolesForApplication(String authToken, String realmName, String clientApp, String appName, List<RoleRepresentation> roles) throws Exception {
    }

    @Override
    public void removeClientScopeRolesForApplication(String authToken, String realmName, String clientApp, String appName, List<RoleRepresentation> roles) throws Exception {
    }

    @Override
    public List<RoleRepresentation> availableClientScopeRolesForApplication(String authToken, String realmName, String clientApp, String appName) throws Exception {
        return null;
    }
}
