package io.liveoak.container.zero;

import java.io.File;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationRegistry;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Ken Finnigan
 */
public class LocalApplicationsResource implements RootResource, SynchronousResource {

    public LocalApplicationsResource(InternalApplicationRegistry applicationRegistry) {
        this.applicationRegistry = applicationRegistry;
    }

    public void parent(Resource parent) {
        this.parent = parent;
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
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        File localDir = null;
        String localPath = (String) state.getProperty("localPath");
        if (localPath != null) {
            localDir = new File(localPath);
        }

        // Copy from 'localPath' to application path

        InternalApplication app = this.applicationRegistry.createApplication(state.id(), (String) state.getProperty("name"), localDir);
        responder.resourceCreated(app.resource());
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        sink.close();
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        responder.readNotSupported(this);
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        responder.updateNotSupported(this);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        responder.deleteNotSupported(this);
    }

    private Resource parent;
    private final InternalApplicationRegistry applicationRegistry;
}
