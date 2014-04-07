package io.liveoak.wildfly;

import org.jboss.as.network.SocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.net.InetSocketAddress;

/**
 * @author Bob McWhirter
 */
public class LiveOakSocketBindingService implements Service<InetSocketAddress> {
    @Override
    public void start(StartContext context) throws StartException {

    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public InetSocketAddress getValue() throws IllegalStateException, IllegalArgumentException {
        return this.socketBindingInjector.getValue().getSocketAddress();
    }

    public Injector<SocketBinding> socketBindingInjector() {
        return this.socketBindingInjector;
    }

    private InjectedValue<SocketBinding> socketBindingInjector = new InjectedValue<>();
}
