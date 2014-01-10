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
import org.keycloak.representations.idm.RealmRepresentation;
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
    private RealmRepresentation realmRepresentation;

    public KeycloakRootResource(String id) {
        this.id = id;

        configuration = new KeycloakConfigResource(this);
    }

    public String getRealm() {
        return realm;
    }

    public PublicKey getPublicKey() {
        return publicKey;
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

    public RealmRepresentation getRealmRepresentation() {
        return realmRepresentation;
    }

    public void setRealmRepresentation(RealmRepresentation realmRepresentation) {
        this.realmRepresentation = realmRepresentation;
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

            if (realmRepresentation == null) {
                throw new InitializationException("realmRepresentation is null");
            } else {
                try {
                    initRealm();
                } catch (Exception e) {
                    throw new InitializationException(e);
                }
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

    private RealmModel initRealm() throws Exception {
        KeycloakSession session = createSession();
        session.getTransaction().begin();

        try {
            RealmManager realmManager = new RealmManager(session);

            String realmName = realmRepresentation.getRealm();
            RealmModel realmModel = realmManager.getRealm(realmName);
            if (realmModel == null) {
                realmModel = realmManager.createRealm(realmName);
                realmManager.importRealm(realmRepresentation, realmModel);
                logger().infof("Realm %s imported successfully", realmName);
            } else {
                logger().infof("Realm %s already exists. Import skipped", realmName);
            }

            this.realm = realmName;
            this.publicKey = realmModel.getPublicKey();

            session.getTransaction().commit();

            return realmModel;
        } catch (Throwable t) {
            session.getTransaction().rollback();
            throw t;
        } finally {
            session.close();
        }
    }

    public Logger logger() {
        return log;
    };
}

