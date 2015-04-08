package io.liveoak.security.client.service;

import io.liveoak.security.client.SecurityClient;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Ken Finnigan
 */
public class SecurityClientService implements Service<SecurityClient> {
    @Override
    public void start(StartContext context) throws StartException {
        client = new SecurityClient(sedcurityBaseUrl.getValue());
    }

    @Override
    public void stop(StopContext context) {
        client = null;
    }

    @Override
    public SecurityClient getValue() throws IllegalStateException, IllegalArgumentException {
        return client;
    }

    public Injector<String> securityBaseUrlInjector() {
        return this.sedcurityBaseUrl;
    }

    private SecurityClient client;
    private InjectedValue<String> sedcurityBaseUrl = new InjectedValue<>();
}
