package io.liveoak.keycloak.server;

import org.keycloak.models.ApplicationModel;
import org.keycloak.models.ClaimMask;
import org.keycloak.models.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.managers.ApplicationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.KeycloakApplication;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.io.FileNotFoundException;

public class KeycloakServerApplication extends KeycloakApplication {

    static {
        Config.setAdminRealm("liveoak-admin");
        Config.setModelProvider("mongo");
        Config.setThemeDefault("liveoak");
    }

    public KeycloakServerApplication(@Context ServletContext servletContext) throws FileNotFoundException {
        super(servletContext);

        classes.add(KeycloakAdminCorsFilter.class);
        classes.add(KeycloakAdminCorsPreflightFilter.class);

        KeycloakSession session = factory.createSession();
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

        //TODO: Setting for 30min instead of 1min due to bug in KC. Should be reverted back - LIVEOAK-288
        adminRealm.setAccessTokenLifespan(1800);

        ApplicationModel consoleApp = adminRealm.getApplicationByName("console");
        if (consoleApp == null) {
            consoleApp = new ApplicationManager(manager).createApplication(adminRealm, "console");
            consoleApp.setPublicClient(true);

            consoleApp.setAllowedClaimsMask(ClaimMask.USERNAME);

            // TODO Remove once KC supports relative redirect_uri
            String baseUrl = "http://" + System.getProperty("jboss.bind.address", "localhost");
            String port = System.getProperty("jboss.http.port", "8080");
            if (!port.equals("80")) {
                baseUrl += ":" + port;
            }

            consoleApp.addRedirectUri(baseUrl + "/admin");
            consoleApp.addRedirectUri(baseUrl + "/admin/");
            consoleApp.addWebOrigin(baseUrl);
            consoleApp.setBaseUrl(baseUrl + "/admin/");

            adminRealm.addScopeMapping(consoleApp, adminRealm.getRole("admin"));
        }

        RealmModel appsRealm = manager.getRealm("liveoak-apps");
        if (appsRealm == null) {
            RealmManager realmManager = new RealmManager(session);
            RealmModel realm = realmManager.createRealm("liveoak-apps");
            realm.setEnabled(true);
            realm.setRegistrationAllowed(true);
            realm.setSslNotRequired(true);
            realm.addRequiredCredential(CredentialRepresentation.PASSWORD);

            realm.setCentralLoginLifespan(3000);

            //TODO: Setting for 30min instead of 1min due to bug in KC. Should be reverted back - LIVEOAK-288
            realm.setAccessTokenLifespan(1800);

            realm.setRefreshTokenLifespan(3600);
            realm.setAccessCodeLifespan(60);
            realm.setAccessCodeLifespanUserAction(300);

            manager.generateRealmKeys(realm);
        }
    }

}
