package io.liveoak.container.extension.reconfig;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class MockConsumerService implements Service<MockClient> {

    @Override
    public void start(StartContext context) throws StartException {
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public MockClient getValue() throws IllegalStateException, IllegalArgumentException {
        return this.clientInjector.getValue();
    }

    public Injector<MockClient> clientInjector() {
        return this.clientInjector;
    }

    private InjectedValue<MockClient> clientInjector = new InjectedValue<>();
}
