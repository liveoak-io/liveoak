package io.liveoak.wildfly;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class ConfDirectoryPathService implements Service<String> {

    @Override
    public void start(StartContext context) throws StartException {

    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public String getValue() throws IllegalStateException, IllegalArgumentException {
        return new File( this.jbossHomeInjector.getValue(), "conf" ).getAbsolutePath();
    }

    public Injector<String> jbossHomeInjector() {
        return this.jbossHomeInjector;
    }

    private InjectedValue<String> jbossHomeInjector = new InjectedValue<>();
}
