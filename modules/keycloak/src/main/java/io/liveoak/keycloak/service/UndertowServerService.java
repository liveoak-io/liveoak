package io.liveoak.keycloak.service;

import io.liveoak.keycloak.UndertowServer;
import io.liveoak.spi.container.Address;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class UndertowServerService implements Service<UndertowServer> {

    @Override
    public void start(StartContext context) throws StartException {
        Address address = this.addressInjector.getValue();
        this.server = new UndertowServer( address.host(), address.portUndertow() );
        this.server.start();
    }

    @Override
    public void stop(StopContext context) {
        this.server.stop();
    }

    @Override
    public UndertowServer getValue() throws IllegalStateException, IllegalArgumentException {
        return this.server;
    }

    public Injector<Address> addressInjector(){
        return this.addressInjector;
    }

    private InjectedValue<Address> addressInjector = new InjectedValue<>();

    private UndertowServer server;
}
