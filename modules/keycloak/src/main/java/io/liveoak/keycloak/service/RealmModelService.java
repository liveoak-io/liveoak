package io.liveoak.keycloak.service;

import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.managers.RealmManager;

/**
 * @author Bob McWhirter
 */
public class RealmModelService implements Service<RealmModel> {

    private static final Logger log = Logger.getLogger("io.liveoak.keycloak");

    @Override
    public void start(StartContext context) throws StartException {

        KeycloakSession session = this.sessionFactoryInjector.getValue().createSession();
        session.getTransaction().begin();

        try {
            RealmManager realmManager = new RealmManager(session);
            RealmRepresentation realmRepresentation = this.realmRepresentationInjector.getValue();

            String realmName = realmRepresentation.getRealm();
            RealmModel realmModel = realmManager.getRealm(realmName);
            if (realmModel == null) {
                realmModel = realmManager.createRealm(realmName);
                realmManager.importRealm(realmRepresentation, realmModel);
                this.realmModel = realmModel;
                log.infof("Realm %s imported successfully", realmName);
            } else {
                realmManager.updateRealm( realmRepresentation, realmModel );
                this.realmModel = realmModel;
                log.infof("Realm %s updated successfully", realmName);
            }

            session.getTransaction().commit();
        } catch (Throwable t) {
            session.getTransaction().rollback();
            throw t;
        } finally {
            session.close();
        }

    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public RealmModel getValue() throws IllegalStateException, IllegalArgumentException {
        return this.realmModel;
    }

    public Injector<KeycloakSessionFactory> sessionFactoryInjector() {
        return this.sessionFactoryInjector;
    }

    public Injector<RealmRepresentation> realmRepresentationInjector() {
        return this.realmRepresentationInjector;
    }

    private InjectedValue<KeycloakSessionFactory> sessionFactoryInjector = new InjectedValue<>();
    private InjectedValue<RealmRepresentation> realmRepresentationInjector = new InjectedValue<>();

    private RealmModel realmModel;

}
