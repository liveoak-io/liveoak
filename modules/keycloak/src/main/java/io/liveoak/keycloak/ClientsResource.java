package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import org.keycloak.representations.idm.ApplicationRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ClientsResource implements Resource {

    private Resource parent;
    private final KeycloakAdmin admin;
    private final RealmRepresentation realm;

    public ClientsResource(Resource parent, KeycloakAdmin admin, RealmRepresentation realm) {
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
        return "clients";
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        for (ApplicationRepresentation app : admin.getApplications(realm.getRealm())) {
            if (isClient(app)) {
                sink.accept(new ClientResource(this, admin, realm, app));
            }
        }
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        ApplicationRepresentation app = admin.getApplication(realm.getRealm(), id);
        if (app != null && isClient(app)) {
            responder.resourceRead(new ClientResource(this, admin, realm, app));
        } else {
            responder.noSuchResource(id);
        }
    }

    private boolean isClient(ApplicationRepresentation app) throws InterruptedException {
        return app.isPublicClient();
    }

}

