package io.liveoak.container.extension;

import java.util.Properties;

import io.liveoak.container.tenancy.ConfigurationManager;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.RootResource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class SystemResourceWrappingResource extends ConfigResourceWrappingResource {

    public SystemResourceWrappingResource(ConfigurationManager configManager, RootResource delegate, Properties envProps, Client client) {
        super(configManager, delegate, envProps, client);
    }

}
