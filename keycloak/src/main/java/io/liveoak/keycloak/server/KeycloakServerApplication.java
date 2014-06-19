package io.liveoak.keycloak.server;

import io.liveoak.keycloak.theme.LiveOakLoginThemeProviderFactory;
import org.jboss.resteasy.core.Dispatcher;
import org.keycloak.Config;
import org.keycloak.models.ApplicationModel;
import org.keycloak.models.AuthenticationProviderModel;
import org.keycloak.models.ClaimMask;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderSession;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.managers.ApplicationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.KeycloakApplication;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class KeycloakServerApplication extends KeycloakApplication {

    public KeycloakServerApplication(@Context ServletContext context, @Context Dispatcher dispatcher) throws FileNotFoundException {
        super(context, dispatcher);

        ProviderSession providerSession = getProviderSessionFactory().createSession();
        KeycloakSession session = providerSession.getProvider(KeycloakSession.class);
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

            consoleApp.setAllowedClaimsMask(ClaimMask.USERNAME);
            consoleApp.addRedirectUri("/admin");
            consoleApp.addRedirectUri("/admin/");
            consoleApp.setBaseUrl("/admin/");

            consoleApp.addScopeMapping(adminRealm.getRole("admin"));
        }

        RealmModel appsRealm = manager.getRealm("liveoak-apps");
        if (appsRealm == null) {
            RealmManager realmManager = new RealmManager(session);
            RealmModel realm = realmManager.createRealm("liveoak-apps");

            realm.setAuthenticationProviders(Arrays.asList(AuthenticationProviderModel.DEFAULT_PROVIDER));

            realm.setEnabled(true);
            realm.setRegistrationAllowed(true);
            realm.setSslNotRequired(true);
            realm.addRequiredCredential(CredentialRepresentation.PASSWORD);

            realm.setSsoSessionMaxLifespan(36000);

            realm.setAccessTokenLifespan(300);

            realm.setSsoSessionIdleTimeout(600);
            realm.setAccessCodeLifespan(60);
            realm.setAccessCodeLifespanUserAction(300);

            manager.generateRealmKeys(realm);

            realm.setLoginTheme(LiveOakLoginThemeProviderFactory.ID);
            realm.setAccountTheme(LiveOakLoginThemeProviderFactory.ID);
            adminRealm.setLoginTheme(LiveOakLoginThemeProviderFactory.ID);
            adminRealm.setAccountTheme(LiveOakLoginThemeProviderFactory.ID);
        }
    }

}
