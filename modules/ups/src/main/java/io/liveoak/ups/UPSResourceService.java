package io.liveoak.ups;

import io.liveoak.spi.container.SubscriptionManager;
import io.liveoak.ups.resource.UPSRootResource;
import io.liveoak.ups.resource.config.UPSRootConfigResource;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class UPSResourceService implements Service<UPSRootResource> {

    String id;
    private UPSRootResource upsRootResource;

    public UPSResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start( StartContext context ) throws StartException {
        this.upsRootResource = new UPSRootResource(this.id, configResourceInjector.getValue(), subscriptionManagerInjector.getValue());
    }

    @Override
    public void stop( StopContext context ) {
        this.upsRootResource = null;
    }

    @Override
    public UPSRootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return upsRootResource;
    }


    public InjectedValue<SubscriptionManager> subscriptionManagerInjector = new InjectedValue<SubscriptionManager>();

    public InjectedValue<UPSRootConfigResource> configResourceInjector = new InjectedValue<UPSRootConfigResource>();
}
