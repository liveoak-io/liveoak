package io.liveoak.keycloak.server;

import java.io.FileNotFoundException;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import io.liveoak.keycloak.theme.LiveOakLoginThemeProviderFactory;
import org.jboss.resteasy.core.Dispatcher;
import org.keycloak.Config;
import org.keycloak.enums.SslRequired;
import org.keycloak.models.ApplicationModel;
import org.keycloak.models.ClaimMask;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.managers.ApplicationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.KeycloakApplication;

public class KeycloakServerApplication extends KeycloakApplication {

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

        ApplicationModel consoleApp = adminRealm.getApplicationByName("console");
        if (consoleApp == null) {
            consoleApp = new ApplicationManager(manager).createApplication(adminRealm, "console");
            consoleApp.setPublicClient(true);

            consoleApp.setAllowedClaimsMask(ClaimMask.ALL);
            consoleApp.addRedirectUri("/admin");
            consoleApp.addRedirectUri("/admin/");
            consoleApp.setBaseUrl("/admin/");

            consoleApp.addScopeMapping(adminRealm.getRole("admin"));
        }

        RealmModel appsRealm = manager.getRealm("liveoak-apps");
        if (appsRealm == null) {
            RealmManager realmManager = new RealmManager(session);
            RealmModel realm = realmManager.createRealm("liveoak-apps");

            realm.setEnabled(true);
            realm.setRegistrationAllowed(true);
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
        }
    }

}
