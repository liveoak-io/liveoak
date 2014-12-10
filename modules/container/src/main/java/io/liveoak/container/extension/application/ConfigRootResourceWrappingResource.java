package io.liveoak.container.extension.application;

import java.io.File;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.common.util.FileHelper;
import io.liveoak.container.extension.ConfigVersioningResponder;
import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.resource.config.ConfigRootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.spi.util.ObjectMapperFactory;

/**
 * @author Ken Finnigan
 */
public class ConfigRootResourceWrappingResource extends AdminResourceWrappingResource {

    public ConfigRootResourceWrappingResource(InternalApplicationExtension extension, ApplicationConfigurationManager configManager, RootResource delegate, Properties envProps, Client client) {
        super(extension, configManager, delegate, envProps, client);
    }

    @Override
    public void createMember(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        delegate().createMember(ctx, state,
                new ConfigResourcePersistingResponder(state, delegate().baseConfig(extension.application().configurationDirectory()),
                        new ConfigVersioningResponder(responder, extension.application().versioned(), extension.application().versionedResourcePath(), client, ctx.securityContext())
                )
        );
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        FileHelper.deleteNonEmpty(delegate().baseConfig(extension.application().configurationDirectory()));

        super.delete(ctx, responder);
    }

    @Override
    public void initializeProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        File childConfig = delegate().baseConfig(extension.application().configurationDirectory());
        if (childConfig.exists()) {
            if (childConfig.isFile()) {
                ObjectMapper mapper = ObjectMapperFactory.create();
                JsonNode children = mapper.readTree(childConfig);
                if (children.isArray()) {
                    children.elements().forEachRemaining(e -> state.addMember(ConversionUtils.convert(e)));
                }
            } else {
                //TODO Handle case where config is a dir
            }
        }
        super.initializeProperties(ctx, state, responder);
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        delegate().readMember(ctx, id, new WrapConfigResourceResponder(responder, extension.application(), client));
    }

    public ConfigRootResource delegate() {
        return (ConfigRootResource) super.delegate();
    }
}
