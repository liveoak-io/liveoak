package io.liveoak.container.tenancy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.liveoak.container.zero.ApplicationExtensionsResource;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.exceptions.PropertyException;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ApplicationResource implements RootResource, SynchronousResource {

    public ApplicationResource(InternalApplication app, ApplicationConfigurationManager configManager, InternalApplicationRegistry appRegistry, Client client) {
        this.app = app;
        this.configManager = configManager;
        this.appRegistry = appRegistry;
        this.client = client;
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
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("name", this.app.name());
        result.put("html-app", this.app.htmlApplicationResourcePath());
        result.put("visible", this.app.visible());
        result.put("version-resource-id", this.app.versionResourceId());
        result.put("directory", this.app.directory().getAbsolutePath());
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

        String versionResourceId = (String) state.getProperty("version-resource-id");
        if (versionResourceId != null) {
            boolean resourceFound = false;
            for (Resource resource : this.extensions.members(ctx)) {
                if (resource.id().equals(versionResourceId)) {
                    resourceFound = true;
                    break;
                }
            }
            if (!resourceFound) {
                throw new PropertyException("No versioning resource found with id: " + versionResourceId);
            }
            this.app.setVersionResourceId(versionResourceId);
        }

        this.configManager.updateApplication(this.app);

        Boolean partOfGitInstallProcess = (Boolean) state.getProperty("git-install-process");
        if (this.app.versioned() && partOfGitInstallProcess == null || !partOfGitInstallProcess) {
            // Commit configuration changes
            //TODO
        }

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
    private Client client;
}
