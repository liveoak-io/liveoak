package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.keycloak.representations.idm.ApplicationRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ApplicationsResource implements Resource {

    private Resource parent;
    private final KeycloakAdmin admin;
    private final RealmRepresentation realm;

    public ApplicationsResource(Resource parent, KeycloakAdmin admin, RealmRepresentation realm) {
        this.parent = parent;
        this.admin = admin;
        this.realm = realm;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return "applications";
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        for (ApplicationRepresentation app : admin.getApplications(realm.getRealm())) {
            if (isApp(app)) {
                sink.accept(new ApplicationResource(this, admin, realm, app));
            }
        }
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        ApplicationRepresentation app = admin.getApplication(realm.getRealm(), id);
        if (app != null && app.isBearerOnly()) {
            responder.resourceRead(new ApplicationResource(this, admin, realm, app));
        } else {
            responder.noSuchResource(id);
        }
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {

    }

    private boolean isApp(ApplicationRepresentation app) throws InterruptedException {
        return app.isBearerOnly();
    }

}

