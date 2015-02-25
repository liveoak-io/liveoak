package io.liveoak.keycloak.server;

import java.io.FileNotFoundException;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import io.liveoak.keycloak.theme.LiveOakLoginThemeProviderFactory;
import org.jboss.resteasy.core.Dispatcher;
import org.keycloak.Config;
import org.keycloak.enums.SslRequired;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ApplicationModel;
import org.keycloak.models.ClaimMask;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OAuthClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.managers.ApplicationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.KeycloakApplication;

public class KeycloakServerApplication extends KeycloakApplication {

    private static final String LIVEOAK_INITIAL_PASSWORD_PARAMETER = "liveoak.initial.password";

    public KeycloakServerApplication(@Context ServletContext context, @Context Dispatcher dispatcher) throws FileNotFoundException {
        super(context, dispatcher);
    }

    @Override
    protected void setupDefaultRealm(String contextPath) {
        super.setupDefaultRealm(contextPath);

        KeycloakSession session = getSessionFactory().create();
        session.getTransaction().begin();
        try {
            configureLiveOakConsole(session);
            session.getTransaction().commit();
        } finally {
            session.close();
        }
    }

    protected void configureLiveOakConsole(KeycloakSession session) {
        RealmManager manager = new RealmManager(session);
        RealmModel adminRealm = manager.getRealm(Config.getAdminRealm());
        UserCredentialModel adminCredential = null;

        ApplicationModel consoleApp = adminRealm.getApplicationByName("console");
        if (consoleApp == null) {
            consoleApp = new ApplicationManager(manager).createApplication(adminRealm, "console");
            consoleApp.setPublicClient(true);

            consoleApp.setAllowedClaimsMask(ClaimMask.ALL);
            consoleApp.addRedirectUri("/admin");
            consoleApp.addRedirectUri("/admin/");
            consoleApp.setBaseUrl("/admin/");

            //check if we should create a customized default password for LiveOak's KeyCloak instance
            String initialPassword = System.getProperty(LIVEOAK_INITIAL_PASSWORD_PARAMETER);
            if (initialPassword != null) {
                //Create a new, initial password for the admin user
                adminCredential = new UserCredentialModel();
                adminCredential.setType(UserCredentialModel.PASSWORD);
                adminCredential.setValue(initialPassword);

                session.users().getUserByUsername("admin", adminRealm).updateCredential(adminCredential);
            }

            consoleApp.addScopeMapping(adminRealm.getRole("admin"));
        }

        RealmModel appsRealm = manager.getRealm("liveoak-apps");
        if (appsRealm == null) {
            RealmManager realmManager = new RealmManager(session);
            RealmModel realm = realmManager.createRealm("liveoak-apps");

            realm.setEnabled(true);
            realm.setRegistrationAllowed(true);
            realm.setPasswordCredentialGrantAllowed(true);
            realm.setSslRequired(SslRequired.EXTERNAL);
            realm.addRequiredCredential(CredentialRepresentation.PASSWORD);

            realm.setSsoSessionMaxLifespan(36000);
            realm.setAccessTokenLifespan(300);
            realm.setSsoSessionIdleTimeout(6000);
            realm.setAccessCodeLifespan(60);
            realm.setAccessCodeLifespanUserAction(300);
            KeycloakModelUtils.generateRealmKeys(realm);

            realm.setLoginTheme(LiveOakLoginThemeProviderFactory.ID);
            realm.setAccountTheme(LiveOakLoginThemeProviderFactory.ID);
            adminRealm.setLoginTheme(LiveOakLoginThemeProviderFactory.ID);
            adminRealm.setAccountTheme(LiveOakLoginThemeProviderFactory.ID);
            adminRealm.setSsoSessionIdleTimeout(6000);

            // Add OAuth client for accessing Keycloak on LiveOak startup
            OAuthClientModel oauthClient = realm.addOAuthClient("liveoak-admin-client");
            oauthClient.setDirectGrantsOnly(true);
            oauthClient.setPublicClient(true);
            oauthClient.setEnabled(true);

            // Add 'realm-admin' role to OAuth client
            ApplicationModel realmApp = realm.getApplicationByName("realm-management");
            RoleModel realmAdminRole = realmApp.getRole(AdminRoles.REALM_ADMIN);
            oauthClient.addScopeMapping(realmAdminRole);

            // Create user for OAuth client usage
            UserModel user = session.users().addUser(realm, "liveoak-server");
            if (adminCredential == null) {
                adminCredential = new UserCredentialModel();
                adminCredential.setType(UserCredentialModel.PASSWORD);
                adminCredential.setValue("password");
            }
            user.updateCredential(adminCredential);
            user.grantRole(realmAdminRole);
            user.setEnabled(true);
        }
    }

}
