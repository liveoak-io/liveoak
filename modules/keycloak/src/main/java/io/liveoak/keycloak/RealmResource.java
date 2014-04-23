package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class RealmResource implements Resource {

    private Resource parent;
    private KeycloakAdmin admin;
    private RealmRepresentation realm;
    private final ApplicationsResource applications;
    private final ClientsResource clients;

    public RealmResource(Resource parent, KeycloakAdmin admin, RealmRepresentation realm) {
        this.parent = parent;
        this.admin = admin;
        this.realm = realm;
        this.applications = new ApplicationsResource(this, admin, realm);
        this.clients = new ClientsResource(this, admin, realm);
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return realm.getRealm();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        sink.accept(applications);
        sink.accept(clients);
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        if (id.equals(applications.id())) {
            responder.resourceRead(applications);
        }
        if (id.equals(clients.id())) {
            responder.resourceRead(clients);
        } else {
            responder.noSuchResource(id);
        }
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("name", realm.getRealm());
        sink.close();
    }

}

