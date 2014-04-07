package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import org.keycloak.representations.idm.ApplicationRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClientResource implements Resource {

    private Resource parent;
    private final KeycloakAdmin admin;
    private final RealmRepresentation realm;
    private final ApplicationRepresentation app;

    public ClientResource(Resource parent, KeycloakAdmin admin, RealmRepresentation realm, ApplicationRepresentation app) {
        this.parent = parent;
        this.admin = admin;
        this.realm = realm;
        this.app = app;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return app.getName();
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("name", app.getName());
        sink.accept("redirect-uris", app.getRedirectUris());
        sink.accept("web-origins", app.getWebOrigins());
        sink.accept("scope", "todo");
        sink.close();
    }
//
//    private Set<String> getScope() {
//        Set<String> scope = new HashSet<>();
//
//        for (RoleModel role : realm.getScopeMappings(application)) {
//            scope.add(role.getName());
//        }
//
//        for (ApplicationModel a : realm.getApplications()) {
//            for (RoleModel role : a.getApplicationScopeMappings(application)) {
//                scope.add(a.getName() + "/" + role.getName());
//            }
//        }
//
//        return scope;
//    }

}

