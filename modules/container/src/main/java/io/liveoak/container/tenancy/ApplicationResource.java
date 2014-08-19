package io.liveoak.container.tenancy;

import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.container.zero.ApplicationExtensionsResource;
import io.liveoak.spi.ApplicationClient;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ApplicationResource implements RootResource, SynchronousResource {

    public ApplicationResource(InternalApplication app, ApplicationConfigurationManager configManager) {
        this.app = app;
        this.configManager = configManager;
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
    public Collection<Resource> members() {
        return Collections.singletonList(this.extensions);
    }

    @Override
    public Resource member(String id) {
        if (id.equals(this.extensions.id())) {
            return this.extensions;
        }
        return null;
    }

    public ApplicationExtensionsResource extensionsResource() {
        return this.extensions;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("name", this.app.name());
        sink.accept("html-app", this.app.htmlApplicationResourcePath());
        sink.accept("visible", this.app.visible());
        sink.accept("directory", this.app.directory().getAbsolutePath());
        sink.accept("clients", this.app.clients());
        sink.close();
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

        ResourceState clientsState = (ResourceState) state.getProperty("clients");
        if (clientsState != null) {
            for (String clientId : clientsState.getPropertyNames()) {
                ObjectNode clientNode = ConversionUtils.convert((ResourceState) clientsState.getProperty(clientId));
                ApplicationClient client = new ApplicationClient() {
                    @Override
                    public String id() {
                        return clientId;
                    }

                    @Override
                    public String type() {
                        return clientNode.get("type").asText();
                    }

                    @Override
                    public String securityKey() {
                        return clientNode.get("security-key").asText();
                    }
                };
                this.app.addClient(client);
            }
        }

        this.configManager.updateApplication(this.app, ConversionUtils.convert(clientsState));

        responder.resourceUpdated(this);
    }

    private Resource parent;
    private InternalApplication app;
    private final ApplicationExtensionsResource extensions;
    private ApplicationConfigurationManager configManager;

}
