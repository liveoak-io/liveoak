package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import org.keycloak.representations.idm.ApplicationRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ApplicationResource implements Resource {

    private Resource parent;
    private final KeycloakAdmin admin;
    private final RealmRepresentation realm;
    private final ApplicationRepresentation app;

    public ApplicationResource(Resource parent, KeycloakAdmin admin, RealmRepresentation realm, ApplicationRepresentation app) {
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
        sink.accept("roles", "todo");
        sink.accept("default-roles", app.getDefaultRoles());
        sink.close();
    }

//    private List<Map<String, String>> getRoles() {
//        List<Map<String, String>> roles = new LinkedList<>();
//        for (RoleModel r : app.getRoles()) {
//            Map<String, String> m = new HashMap<>();
//            m.put("name", r.getName());
//            m.put("description", r.getDescription());
//            roles.add(m);
//        }
//        return roles;
//    }

}

