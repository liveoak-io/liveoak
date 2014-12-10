package io.liveoak.application.clients.service;

import io.liveoak.application.clients.ApplicationClientsResource;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Ken Finnigan
 */
public class ApplicationClientsResourceService implements Service<RootResource> {
    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new ApplicationClientsResource();
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public RootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    private RootResource resource;
}
