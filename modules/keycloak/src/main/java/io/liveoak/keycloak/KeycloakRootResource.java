package io.liveoak.keycloak;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.ApplicationRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.services.managers.ApplicationManager;
import org.keycloak.services.managers.RealmManager;

import java.lang.reflect.Method;
import java.security.PublicKey;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakRootResource implements RootResource {

    private static final Logger log = Logger.getLogger("io.liveoak.keycloak");

    private String id;
    private UndertowServer undertow;
    private String host = "localhost";
    private int port = 8383;
    private KeycloakServer server;
    private String realm;
    private PublicKey publicKey;
    private final KeycloakConfigResource configuration;
    private ApplicationRepresentation app;

    public KeycloakRootResource(String id) {
        this.id = id;

        configuration = new KeycloakConfigResource(this);
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public KeycloakSession createSession() {
        return server.getKeycloakSessionFactory().createSession();
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            // TODO Remove once fixed in Keycloak
            Thread.currentThread().setContextClassLoader(KeycloakServer.class.getClassLoader());
            log.infof("Going to bootstrap undertow on %s:%d", host, port);
            undertow = new UndertowServer(host, port);
            server = new KeycloakServer(undertow);
            try {
                undertow.start();
                server.start();
            } catch (Throwable t) {
                throw new InitializationException(t);
            }

            try {
                initRealm(realm);
            } catch (Exception e) {
                throw new InitializationException(e);
            }

            if (app != null) {
                initApp(app);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    @Override
    public void destroy() {
        server.stop();
        undertow.stop();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) {
        switch (id) {
            case "token-info":
                responder.resourceRead(new TokenResource(this));
                return;
            default:
                responder.noSuchResource(id);
        }
    }

    @Override
    public Resource configuration() {
        return configuration;
    }


    private RealmModel initRealm(String name) throws Exception {
        KeycloakSession session = createSession();
        session.getTransaction().begin();

        try {
            RealmManager realmManager = new RealmManager(session);

            RealmModel realmModel = realmManager.getRealm(name);
            if (realmModel == null) {
                realmModel = realmManager.createRealm(name);
                realmModel.setEnabled(true);
                realmModel.setAccessCodeLifespan(10);
                realmModel.setAccessCodeLifespanUserAction(600);
                realmModel.setTokenLifespan(3600); // Refresh tokens are not supported atm so need long lifespan on tokens
                realmModel.setCookieLoginAllowed(true);
                realmModel.setSslNotRequired(true);
                realmModel.setRegistrationAllowed(true);
                realmModel.setResetPasswordAllowed(true);

                realmManager.generateRealmKeys(realmModel);

                realmModel.addRequiredCredential(CredentialRepresentation.PASSWORD);
                realmModel.addRequiredResourceCredential(CredentialRepresentation.PASSWORD);

                // TODO Fix when KC is updated (RealmManager.enableAccountManagement is public in master)
                Method enableAccountManagement = RealmManager.class.getDeclaredMethod("enableAccountManagement", new Class[]{RealmModel.class});
                enableAccountManagement.setAccessible(true);
                enableAccountManagement.invoke(realmManager, realmModel);
            }

            this.publicKey  = realmModel.getPublicKey();

            session.getTransaction().commit();

            return realmModel;
        } catch (Throwable t) {
            session.getTransaction().rollback();
            throw t;
        } finally {
            session.close();
        }
    }

    private void initApp(ApplicationRepresentation appRep) {
        KeycloakSession session = createSession();
        session.getTransaction().begin();

        try {
            RealmManager realmManager = new RealmManager(session);
            RealmModel realmModel = realmManager.getRealm(realm);

            ApplicationManager appManager = new ApplicationManager(realmManager);

            if (!realmModel.getApplicationNameMap().containsKey(appRep.getName())) {
                appManager.createApplication(realmModel, appRep);
            }

            session.getTransaction().commit();
        } catch (Throwable t) {
            session.getTransaction().rollback();
            throw t;
        } finally {
            session.close();
        }
    }

    public void setApp(ApplicationRepresentation app) {
        this.app = app;
    }

    public ApplicationRepresentation getApp() {
        return app;
    }

    public Logger logger() {
        return log;
    };
}

