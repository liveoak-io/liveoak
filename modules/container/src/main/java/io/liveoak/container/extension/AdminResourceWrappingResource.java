package io.liveoak.container.extension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.liveoak.common.util.JsonFilterUtils;
import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.DelegatingRootResource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class AdminResourceWrappingResource extends DelegatingRootResource {

    public AdminResourceWrappingResource(InternalApplicationExtension extension, ApplicationConfigurationManager configManager, RootResource delegate, Properties envProps) {
        super(delegate);
        this.extension = extension;
        this.configManager = configManager;
        this.environmentProperties = envProps;
    }

    public String type() {
        return this.extension.extensionId();
    }

    public ApplicationConfigurationManager configurationManager() {
        return this.configManager;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        boolean runtimeValuePresent = ctx.resourceParams().value("runtime") != null;
        boolean runtimeRequested = runtimeValuePresent ? Boolean.parseBoolean(ctx.resourceParams().value("runtime")) : ctx.resourceParams().names().contains("runtime");

        // If runtime is not set, we replace the config values
        if (!runtimeRequested) {
            sink.replaceConfig((names, object) -> {
                if (configValuesWithVariables.containsKey(names[0])) {
                    ConfigValue configValue = configValuesWithVariables.get(names[0]);
                    boolean matched = false;

                    // If there's no parents to check, return the value
                    if (names.length == 1 && configValue.parents().length == 0) {
                        return configValue.value();
                    }

                    // Ensure the parent hierarchy has same depth before comparing
                    if (names.length == configValue.parents().length + 1) {
                        int index = 1;
                        for (String parent : configValue.parents()) {
                            if (names[index++] != parent) {
                                break;
                            }
                        }
                        matched = true;
                    }

                    return matched ? configValue.value() : object;
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
        updateConfigEnvVars(state);
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        cleanup(state);
        delegate().updateProperties(ctx, filter(state), new ResourceConfigPersistingResponder(this, state, responder));
        configValuesWithVariables = new HashMap<>();
        updateConfigEnvVars(state);
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.extension.remove();
        // TODO should we call delete() on this resource?  I think yes.
        //this.delegate.delete(ctx, responder);
        this.configManager.removeResource(this.id());
        responder.resourceDeleted(this.delegate());
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

    private void updateConfigEnvVars(ResourceState state, String... parents) {
        state.getPropertyNames().forEach(name -> handleConfigObject(name, state.getProperty(name), parents));
    }

    private void handleConfigObject(String name, Object value, String... parents) {
        if (value != null) {
            if (value instanceof ResourceState) {
                updateConfigEnvVars((ResourceState) value, append(name, parents));
            } else if (value instanceof List) {
                ((List) value).forEach(obj->handleConfigObject(name, obj, parents));
            } else {
                String val = value.toString();
                int start = val.indexOf("${");
                int end = val.indexOf("}", start);
                if (end > start) {
                    configValuesWithVariables.put(name, new ConfigValue(value, parents));
                }
            }
        }
    }

    private String[] append(String newParent, String... parents) {
        String[] newArray = new String[parents.length + 1];
        System.arraycopy(parents, 0, newArray, 0, parents.length);
        newArray[parents.length] = newParent;
        return newArray;
    }

    private class ConfigValue {
        private Object configValue;
        private String[] parents;

        ConfigValue(Object configValue, String... parents) {
            this.configValue = configValue;
            this.parents = parents;
        }

        public Object value() {
            return this.configValue;
        }

        public String[] parents() {
            return this.parents;
        }
    }

    private final InternalApplicationExtension extension;
    private final ApplicationConfigurationManager configManager;
    private Properties environmentProperties;
    private Map<String, ConfigValue> configValuesWithVariables = new HashMap<>();

}
