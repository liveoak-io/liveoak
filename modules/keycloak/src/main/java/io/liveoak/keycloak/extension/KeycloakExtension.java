package io.liveoak.keycloak.extension;

import io.liveoak.keycloak.KeycloakServices;
import io.liveoak.keycloak.service.KeycloakResourceService;
import io.liveoak.keycloak.service.KeycloakSystemResourceService;
import io.liveoak.keycloak.service.RealmModelService;
import io.liveoak.keycloak.service.RealmRepresentationService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import io.liveoak.spi.resource.async.DefaultRootResource;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceController;
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

        KeycloakSystemResourceService system = new KeycloakSystemResourceService( context.id() );

        target.addService(LiveOak.systemResource( context.id() ), system )
                .install();

        context.mountPrivate( LiveOak.systemResource( context.id() ));
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {

        String appId = context.application().id();

        ServiceTarget target = context.target();

        target.addListener( new AbstractServiceListener<Object>() {
            @Override
            public void transition(ServiceController<?> controller, ServiceController.Transition transition) {
                System.err.println( controller.getName() + " :: " + transition );
            }
        } );


        File realmConfig = new File( context.application().directory(), "keycloak-config.json" );
        RealmRepresentationService realmRepresentation = new RealmRepresentationService( appId );
        target.addService(KeycloakServices.realmRepresentation( appId ), realmRepresentation )
                .addInjection(realmRepresentation.fileInjector(), realmConfig)
                .install();

        RealmModelService realmModel = new RealmModelService();
        target.addService(KeycloakServices.realmModel( appId ), realmModel )
                .addDependency( KeycloakServices.realmRepresentation( appId ), RealmRepresentation.class, realmModel.realmRepresentationInjector() )
                .addDependency( KeycloakServices.sessionFactory( context.extensionId() ), KeycloakSessionFactory.class, realmModel.sessionFactoryInjector() )
                .install();

        KeycloakResourceService resource = new KeycloakResourceService( context.resourceId() );
        target.addService( LiveOak.resource( appId, context.resourceId() ), resource )
                .addDependency(KeycloakServices.realmModel(appId), RealmModel.class, resource.realmModelInjector())
                .install();

        context.mountPublic();

        context.mountPrivate( new DefaultRootResource( context.resourceId() ) );


    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {

    }
}
