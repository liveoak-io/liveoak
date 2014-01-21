package io.liveoak.container.zero.service;

import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalOrganization;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ZeroApplicationDeployer implements Service<InternalApplication> {
    @Override
    public void start(StartContext context) throws StartException {
        try {
            this.application = this.organizationInjector.getValue().createApplication( "zero", "Zero" );
            System.err.println( "** Zero application deployed" );
        } catch (InterruptedException e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public InternalApplication getValue() throws IllegalStateException, IllegalArgumentException {
        return this.application;
    }

    public Injector<InternalOrganization> organizationInjector() {
        return this.organizationInjector;
    }

    private InjectedValue<InternalOrganization> organizationInjector = new InjectedValue<>();
    private InternalApplication application;
}
