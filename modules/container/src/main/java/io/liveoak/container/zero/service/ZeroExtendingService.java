package io.liveoak.container.zero.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.tenancy.InternalApplication;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class ZeroExtendingService implements Service<Void> {

    @Override
    public void start(StartContext context) throws StartException {
        System.err.println( "EXTENDING APP-ZERO" );
        ObjectNode cssConfig = JsonNodeFactory.instance.objectNode();
        cssConfig.put("directory", System.getProperty("css.dir"));

        try {
            this.applicationInjector.getValue().extend("filesystem", "css", cssConfig);
        } catch (InterruptedException e) {
            throw new StartException(e);
        }

    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<InternalApplication> applicationInjector() {
        return this.applicationInjector;
    }

    private InjectedValue<InternalApplication> applicationInjector = new InjectedValue<>();
}
