package io.liveoak.keycloak.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.keycloak.UndertowConfig;
import io.liveoak.keycloak.UndertowServer;
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
        UndertowConfig config = this.configurationInjector.getValue();
        this.server = new UndertowServer( config.host(), config.port() );
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

    public Injector<UndertowConfig> configurationInjector(){
        return this.configurationInjector;
    }

    private InjectedValue<UndertowConfig> configurationInjector = new InjectedValue<>();

    private UndertowServer server;
}
