package io.liveoak.security.client.service;

import io.liveoak.security.client.DirectAccessClient;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Ken Finnigan
 */
public class DirectAccessClientService implements Service<DirectAccessClient> {
    @Override
    public void start(StartContext context) throws StartException {
        client = new DirectAccessClient(securityBaseUrl.getValue());
    }

    @Override
    public void stop(StopContext context) {
        client.shutdown();
        client = null;
    }

    @Override
    public DirectAccessClient getValue() throws IllegalStateException, IllegalArgumentException {
        return client;
    }


    public Injector<String> securityBaseUrlInjector() {
        return this.securityBaseUrl;
    }

    private DirectAccessClient client;
    private InjectedValue<String> securityBaseUrl = new InjectedValue<>();
}
