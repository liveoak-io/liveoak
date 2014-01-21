package io.liveoak.container.extension.reconfig;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ClientService implements Service<MockClient> {
    @Override
    public void start(StartContext context) throws StartException {
        this.client = new MockClient( this.configInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.client = null;
    }

    @Override
    public MockClient getValue() throws IllegalStateException, IllegalArgumentException {
        return this.client;
    }

    public Injector<ObjectNode> configInjector() {
        return this.configInjector;
    }

    private InjectedValue<ObjectNode> configInjector = new InjectedValue<>();
    private MockClient client;
}
