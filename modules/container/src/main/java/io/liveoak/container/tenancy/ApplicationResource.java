package io.liveoak.container.tenancy;

import java.util.Collection;
import java.util.Collections;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.zero.ApplicationExtensionsResource;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ApplicationResource implements RootResource, SynchronousResource {

    public ApplicationResource(InternalApplication app, ApplicationConfigurationManager configManager, InternalApplicationRegistry appRegistry) {
        this.app = app;
        this.configManager = configManager;
        this.appRegistry = appRegistry;
        this.extensions = new ApplicationExtensionsResource(this, "resources");
    }

    @Override
    public String id() {
        return this.app.id();
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    public InternalApplication application() {
        return this.app;
    }

    @Override
    public Collection<Resource> members(RequestContext ctx) {
        return Collections.singletonList(this.extensions);
    }

    @Override
    public Resource member(RequestContext ctx, String id) {
        if (id.equals(this.extensions.id())) {
            return this.extensions;
        }
        return null;
    }

    public ApplicationExtensionsResource extensionsResource() {
        return this.extensions;
    }

    @Override
    public ResourceState properties(RequestContext ctx) throws Exception {
        ResourceState result = new DefaultResourceState();
        result.putProperty("name", this.app.name());
        result.putProperty("html-app", this.app.htmlApplicationResourcePath());
        result.putProperty("visible", this.app.visible());
        result.putProperty("directory", this.app.directory().getAbsolutePath());
        return result;
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        String name = (String) state.getProperty("name");
        if (name != null && !name.isEmpty()) {
            this.app.setName(name);
        }

        Boolean visible = (Boolean) state.getProperty("visible");
        if (visible != null) {
            this.app.setVisible(visible);
        }

        String htmlPath = (String) state.getProperty("html-app");
        if (htmlPath != null) {
            this.app.setHtmlApplicationPath(htmlPath);
        }

        this.configManager.updateApplication(this.app);

        responder.resourceUpdated(this);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.appRegistry.removeApplication(id());
        responder.resourceDeleted(this);
    }

    private Resource parent;
    private InternalApplication app;
    private final ApplicationExtensionsResource extensions;
    private ApplicationConfigurationManager configManager;
    private InternalApplicationRegistry appRegistry;
}
