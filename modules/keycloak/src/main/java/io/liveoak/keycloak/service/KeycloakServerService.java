package io.liveoak.keycloak.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.keycloak.KeycloakServer;
import io.liveoak.keycloak.UndertowServer;
import io.liveoak.spi.InitializationException;
import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.io.File;
import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class KeycloakServerService implements Service<KeycloakServer> {

    private static final Logger log = Logger.getLogger("io.liveoak.keycloak");

    public KeycloakServerService() {
    }

    @Override
    public void start(StartContext context) throws StartException {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            // TODO Remove once fixed in Keycloak
            Thread.currentThread().setContextClassLoader(KeycloakServer.class.getClassLoader());
            UndertowServer undertow = this.undertowServerInjector.getValue();
            this.server = new KeycloakServer(undertow);
            this.server.start();
        } catch (Throwable t) {
            throw new StartException(t);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    @Override
    public void stop(StopContext context) {
        this.server.stop();
    }

    @Override
    public KeycloakServer getValue() throws IllegalStateException, IllegalArgumentException {
        return this.server;
    }

    public Injector<UndertowServer> undertowServerInjector() {
        return this.undertowServerInjector;
    }

    private InjectedValue<UndertowServer> undertowServerInjector = new InjectedValue<>();
    private KeycloakServer server;
}
