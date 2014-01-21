package io.liveoak.keycloak.extension;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.keycloak.KeycloakServer;
import io.liveoak.keycloak.KeycloakServices;
import io.liveoak.keycloak.UndertowServer;
import io.liveoak.keycloak.service.*;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceTarget;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.RealmRepresentation;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class KeycloakExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {

        ServiceTarget target = context.target();

        UndertowServerService undertow = new UndertowServerService();

        target.addService(KeycloakServices.UNDERTOW, undertow )
                .addDependency( context.configurationServiceName(), ObjectNode.class, undertow.configurationInjector() )
                .install();

        KeycloakServerService keycloak = new KeycloakServerService();

        target.addService(KeycloakServices.KEYCLOAK, keycloak )
                .addDependency(KeycloakServices.UNDERTOW, UndertowServer.class, keycloak.undertowServerInjector())
                .install();

        KeycloakSessionFactoryService sessionFactory = new KeycloakSessionFactoryService();
        target.addService(KeycloakServices.SESSION_FACTORY, sessionFactory )
                .addDependency(KeycloakServices.KEYCLOAK, KeycloakServer.class, sessionFactory.keycloakServerInjector() )
                .install();
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        String orgId = context.application().organization().id();
        String appId = context.application().id();

        ServiceTarget target = context.target();

        File realmConfig = new File( context.application().directory(), "keycloak-config.json" );
        RealmRepresentationService realmRepresentation = new RealmRepresentationService( orgId, appId );
        target.addService(KeycloakServices.realmRepresentation( orgId, appId ), realmRepresentation )
                .addInjection(realmRepresentation.fileInjector(), realmConfig)
                .install();

        RealmModelService realmModel = new RealmModelService();
        target.addService(KeycloakServices.realmModel( orgId, appId ), realmModel )
                .addDependency( KeycloakServices.realmRepresentation( orgId, appId ), RealmRepresentation.class, realmModel.realmRepresentationInjector() )
                .addDependency( KeycloakServices.SESSION_FACTORY, KeycloakSessionFactory.class, realmModel.sessionFactoryInjector() )
                .install();

        KeycloakResourceService resource = new KeycloakResourceService( context.id() );
        target.addService( KeycloakServices.resource( orgId, appId ), resource )
                .addDependency( KeycloakServices.realmModel( orgId, appId ), RealmModel.class, resource.realmModelInjector() )
                .install();

        context.mountPublic( KeycloakServices.resource( orgId, appId ) );
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
