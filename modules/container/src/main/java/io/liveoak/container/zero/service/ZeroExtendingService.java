package io.liveoak.container.zero.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.extension.MountService;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalOrganizationRegistry;
import io.liveoak.container.tenancy.MountPointResource;
import io.liveoak.container.zero.SystemResource;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ZeroExtendingService implements Service<Void> {

    @Override
    public void start(StartContext context) throws StartException {
        ObjectNode cssConfig = JsonNodeFactory.instance.objectNode();
        cssConfig.put("dir", System.getProperty("css.dir"));

        try {
            this.applicationInjector.getValue().extend("css", cssConfig);
        } catch (InterruptedException e) {
            throw new StartException(e);
        }

    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<InternalApplication> applicationInjector() {
        return this.applicationInjector;
    }

    private InjectedValue<InternalApplication> applicationInjector = new InjectedValue<>();
}
