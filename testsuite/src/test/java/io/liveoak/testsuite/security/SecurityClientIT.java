package io.liveoak.testsuite.security;

import java.util.List;
import java.util.Map;

import io.liveoak.keycloak.KeycloakConfig;
import io.liveoak.keycloak.client.SecurityClient;
import io.liveoak.spi.exceptions.ResourceProcessingException;
import org.fest.assertions.Fail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.representations.idm.ApplicationMappingsRepresentation;
import org.keycloak.representations.idm.ApplicationRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class SecurityClientIT extends AbstractLiveOakSecurityTest {

    private static final String APPS_REALM = "liveoak-apps";

    private SecurityClient client;
    private static String token;

    @Before
    public void securityClient() throws Exception {
        KeycloakConfig config = new KeycloakConfig();
        config.setBaseUrl(BASE_SECURITY_ADMIN_URL);
        this.client = new SecurityClient(config);

        if (token == null) {
            browser.navigate().to(BASE_ADMIN_URL);
            performLoginWithConfirm("admin", "admin");
            token = authToken();
        }
    }

    @After
    public void cleanup() throws Exception {
        List<ApplicationRepresentation> apps = this.client.applications(token, APPS_REALM);
        for (ApplicationRepresentation app : apps) {
            if (app.getName().equals("myApp")) {
                this.client.deleteApplication(token, APPS_REALM, "myApp");
            } else if (app.getName().equals("rolesApp")) {
                this.client.deleteApplication(token, APPS_REALM, "rolesApp");
            }
        }
    }

    @Test(expected = ResourceProcessingException.class)
    public void realmApplicationsFailsWithBadRealmName() throws Exception {
        this.client.applications(this.authToken(), "badRealm");
        Fail.fail();
    }

    @Test(expected = ResourceProcessingException.class)
    public void badRealmApplicationRequest() throws Exception {
        KeycloakConfig config = new KeycloakConfig();
        config.setBaseUrl("http://nowhere:8080/auth");
        new SecurityClient(config).applications(this.authToken(), APPS_REALM);
        Fail.fail();
    }

    @Test
    public void realmApplications() throws Exception {
        // Get realm applications - should be 3
        verifyApps(3);

        // Create realm application
        this.client.createApplication(token, APPS_REALM, "myApp");

        // Get realm applications - should be 4
        verifyApps(4);

        // Delete realm application
        this.client.deleteApplication(token, APPS_REALM, "myApp");

        // Get realm applications - should be none
        verifyApps(3);
    }

    @Test
    public void updateApplication() throws Exception {
        // Get realm applications - should be 3
        verifyApps(3);

        // Create realm application
        this.client.createApplication(token, APPS_REALM, "myApp");

        // Get realm applications - should be 4
        verifyApps(4);

        // Get application
        ApplicationRepresentation app = this.client.application(token, APPS_REALM, "myApp");

        assertThat(app.isEnabled()).isTrue();
        app.setEnabled(false);

        // Update application
        this.client.updateApplication(token, APPS_REALM, app);

        // Check update worked
        app = this.client.application(token, APPS_REALM, "myApp");
        assertThat(app.isEnabled()).isFalse();
    }

    @Test(expected = ResourceProcessingException.class)
    public void applicationRolesFailsWithBadAppName() throws Exception {
        this.client.applicationRoles(this.authToken(), APPS_REALM, "badApp");
        Fail.fail();
    }

    @Test(expected = ResourceProcessingException.class)
    public void createApplicationRoleFailsWithBadAppName() throws Exception {
        this.client.createApplicationRole(this.authToken(), APPS_REALM, "badApp", "myRole");
        Fail.fail();
    }

    @Test
    public void applicationRoles() throws Exception {
        this.client.createApplication(token, APPS_REALM, "rolesApp");

        // Get application roles - should be none
        verifyAppRoles("rolesApp", 0);

        // Create application role
        this.client.createApplicationRole(token, APPS_REALM, "rolesApp", "myRole");

        // Get application roles - should be one
        verifyAppRoles("rolesApp", 1);

        // Delete application role
        this.client.deleteApplicationRole(token, APPS_REALM, "rolesApp", "myRole");

        // Get application roles - should be none
        verifyAppRoles("rolesApp", 0);
    }

    @Test
    public void scopeMappings() throws Exception {
        this.client.createApplication(token, APPS_REALM, "myApp");
        this.client.createApplication(token, APPS_REALM, "rolesApp");

        // Switch myApp to not be full scope
        ApplicationRepresentation app = this.client.application(token, APPS_REALM, "myApp");
        app.setFullScopeAllowed(false);
        this.client.updateApplication(token, APPS_REALM, app);

        // Get existing mappings - should be none
        MappingsRepresentation scopeMappings = this.client.clientScopeMappings(token, APPS_REALM, "myApp");
        assertThat(scopeMappings).isNotNull();
        assertThat(scopeMappings.getApplicationMappings()).isNull();
        assertThat(scopeMappings.getRealmMappings()).isNull();

        // Get available roles - should be none
        List<RoleRepresentation> availRoles = this.client.availableClientScopeRolesForApplication(token, APPS_REALM, "myApp", "rolesApp");
        assertThat(availRoles).isNotNull();
        assertThat(availRoles).isEmpty();

        // Create application role
        this.client.createApplicationRole(token, APPS_REALM, "rolesApp", "myRole");

        // Get available roles - should be one
        availRoles = this.client.availableClientScopeRolesForApplication(token, APPS_REALM, "myApp", "rolesApp");
        assertThat(availRoles).isNotNull();
        assertThat(availRoles).isNotEmpty();
        assertThat(availRoles.size()).isEqualTo(1);
        assertThat(availRoles.get(0).getName()).isEqualTo("myRole");

        // Get associated roles for app - should be none
        List<RoleRepresentation> clientRoles = this.client.clientScopeRolesForApplication(token, APPS_REALM, "myApp", "rolesApp");
        assertThat(clientRoles).isNotNull();
        assertThat(clientRoles).isEmpty();

        // Create associated role
        this.client.addClientScopeRolesForApplication(token, APPS_REALM, "myApp", "rolesApp", availRoles);

        // Get associated roles for app - should be one
        clientRoles = this.client.clientScopeRolesForApplication(token, APPS_REALM, "myApp", "rolesApp");
        assertThat(clientRoles).isNotNull();
        assertThat(clientRoles).isNotEmpty();
        assertThat(clientRoles.size()).isEqualTo(1);
        assertThat(clientRoles.get(0).getName()).isEqualTo("myRole");

        // Get existing mappings - should be one
        scopeMappings = this.client.clientScopeMappings(token, APPS_REALM, "myApp");
        assertThat(scopeMappings).isNotNull();
        assertThat(scopeMappings.getApplicationMappings()).isNotEmpty();

        Map<String, ApplicationMappingsRepresentation> appMap = scopeMappings.getApplicationMappings();
        assertThat(appMap).isNotNull();
        assertThat(appMap).isNotEmpty();

        ApplicationMappingsRepresentation mappingsRep = appMap.get("rolesApp");
        assertThat(mappingsRep.getApplication()).isEqualTo("rolesApp");
        assertThat(mappingsRep.getMappings()).isNotNull();
        assertThat(mappingsRep.getMappings()).isNotEmpty();
        assertThat(mappingsRep.getMappings().get(0).getName()).isEqualTo("myRole");

        // Delete associated role
        this.client.removeClientScopeRolesForApplication(token, APPS_REALM, "myApp", "rolesApp", availRoles);

        // Get associated roles for app - should be none
        clientRoles = this.client.clientScopeRolesForApplication(token, APPS_REALM, "myApp", "rolesApp");
        assertThat(clientRoles).isNotNull();
        assertThat(clientRoles).isEmpty();
    }

    private void verifyApps(int count) throws Exception {
        List<ApplicationRepresentation> apps = this.client.applications(token, APPS_REALM);

        // Verify state
        assertThat(apps).isNotNull();
        assertThat(apps).isNotEmpty();
        assertThat(apps.size()).isEqualTo(count);
    }

    private void verifyAppRoles(String app, int count) throws Exception {
        List<RoleRepresentation> roles = this.client.applicationRoles(token, APPS_REALM, app);

        // Verify state
        assertThat(roles).isNotNull();

        if (count == 0) {
            assertThat(roles).isEmpty();
        } else {
            assertThat(roles).isNotEmpty();
        }

        assertThat(roles.size()).isEqualTo(count);
    }
}
