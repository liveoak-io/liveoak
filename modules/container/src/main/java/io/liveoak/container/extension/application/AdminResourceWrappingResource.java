package io.liveoak.container.extension.application;

import java.util.Properties;

import io.liveoak.container.extension.ConfigPersistingWrappingResource;
import io.liveoak.container.tenancy.ApplicationConfigurationManager;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Responder;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class AdminResourceWrappingResource extends ConfigPersistingWrappingResource {

    public AdminResourceWrappingResource(InternalApplicationExtension extension, ApplicationConfigurationManager configManager, RootResource delegate, Properties envProps, Client client) {
        super(configManager, delegate, envProps, client);
        this.extension = extension;
    }

    @Override
    public String extensionId() {
        return this.extension.extensionId();
    }

    @Override
    public boolean resourceVersioned() {
        return extension.application().versioned();
    }

    @Override
    public String versionedResourcePath() {
        return extension.application().versionedResourcePath();
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        this.extension.remove();
        super.delete(ctx, responder);
    }

    protected final InternalApplicationExtension extension;
}
