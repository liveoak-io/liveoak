package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class RealmsResource implements Resource {

    public static final String ID = "realms";

    private Resource parent;
    private KeycloakAdmin admin;

    public RealmsResource(Resource parent, KeycloakAdmin admin) {
        this.parent = parent;
        this.admin = admin;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        for (RealmRepresentation realm : admin.getRealms()) {
            sink.accept(new RealmResource(this, admin, realm));
        }
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        RealmRepresentation realm = admin.getRealm(id);
        if (realm != null) {
            responder.resourceRead(new RealmResource(this, admin, realm));
        } else {
            responder.noSuchResource(id);
        }
    }

}

