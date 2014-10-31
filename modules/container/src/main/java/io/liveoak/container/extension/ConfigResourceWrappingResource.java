package io.liveoak.container.extension;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import io.liveoak.common.util.JsonFilterUtils;
import io.liveoak.common.util.ObjectsTree;
import io.liveoak.container.tenancy.ConfigurationManager;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.DelegatingRootResource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.DelegatingResponder;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ConfigResourceWrappingResource extends DelegatingRootResource {

    public ConfigResourceWrappingResource(ConfigurationManager configManager, RootResource delegate, Properties envProps, Client client) {
        super(delegate);
        this.configManager = configManager;
        this.environmentProperties = envProps;
        this.client = client;
    }

    public String type() {
        return configManager.type();
    }

    public ConfigurationManager configurationManager() {
        return this.configManager;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        boolean runtimeValuePresent = ctx.resourceParams().value("runtime") != null;
        boolean runtimeRequested = runtimeValuePresent ? Boolean.parseBoolean(ctx.resourceParams().value("runtime")) : ctx.resourceParams().names().contains("runtime");

        // If runtime is not set, we replace the config values
        if (!runtimeRequested && this.configValuesTree != null) {
            sink.replaceConfig((names, object) -> {
                ResourcePath path = new ResourcePath(names);
                List<Object> values = this.configValuesTree.objects(path).collect(Collectors.toList());
                if (values != null && values.size() == 1) {
                    return values.get(0);
                }
                return object;
            });
        }

        sink.accept(LiveOak.RESOURCE_TYPE, type());
        super.readProperties(ctx, sink);
    }

    @Override
    public void initializeProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        cleanup(state);
        delegate().initializeProperties(ctx, filter(state), responder);

        this.configValuesTree = new ObjectsTree<>();
        updateConfigEnvVars(state);
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        cleanup(state);
        delegate().updateProperties(ctx, filter(state),
                new ResourceConfigPersistingResponder(this, state, new ExtensionResponder(responder, configManager.versioned(), configManager.versionedResourcePath(), this.client, ctx.securityContext())));

        this.configValuesTree = new ObjectsTree<>();
        updateConfigEnvVars(state);
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        delegate().createMember(ctx, filter(state), new ExtensionResponder(responder, configManager.versioned(), configManager.versionedResourcePath(), this.client, ctx.securityContext()));
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        ExtensionResponder extensionResponder = new ExtensionResponder(responder, configManager.versioned(), configManager.versionedResourcePath(), this.client, ctx.securityContext());//.resourceDeleted(this.delegate());
        delegate().delete(ctx, new DeleteResponder(extensionResponder));
    }

    private class DeleteResponder extends DelegatingResponder {

        public DeleteResponder(Responder delegate) {
            super(delegate);
        }

        @Override
        public void resourceDeleted(Resource resource) {
            try {
                configManager.removeResource(id(), type());
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.resourceDeleted(resource);
        }
    }

    private void cleanup(ResourceState state) {
        //Clean out from the state what we don't care about
        state.removeProperty(LiveOak.ID);
        state.removeProperty(LiveOak.SELF);
        state.removeProperty(LiveOak.RESOURCE_TYPE);
    }

    private ResourceState filter(ResourceState state) {
        return JsonFilterUtils.filter(state, this.environmentProperties);
    }

    private void updateConfigEnvVars(ResourceState state, String... path) {
        state.getPropertyNames().forEach(name -> handleConfigObject(name, state.getProperty(name), path));
    }

    private void handleConfigObject(String name, Object value, String... path) {
        if (value != null) {
            if (value instanceof ResourceState) {
                updateConfigEnvVars((ResourceState) value, append(name, path));
            } else if (value instanceof List) {
                ((List) value).forEach(obj->handleConfigObject(name, obj, path));
            } else {
                String val = value.toString();
                int start = val.indexOf("${");
                int end = val.indexOf("}", start);
                if (end > start) {
                    this.configValuesTree.addObject(value, new ResourcePath(append(name, path)));
                }
            }
        }
    }

    private String[] append(String first, String... path) {
        int newLength = path != null ? path.length + 1 : 1;
        String[] newArray = new String[newLength];
        newArray[0] = first;
        if (newLength != 1) {
            System.arraycopy(path, 0, newArray, 1, newLength - 1);
        }
        return newArray;
    }

    //private final InternalApplicationExtension extension;
    private final ConfigurationManager configManager;
    private Client client;
    private Properties environmentProperties;
    private ObjectsTree<Object> configValuesTree;

}
