package io.liveoak.wildfly;

import java.io.File;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class PropertiesManagerService implements Service<Void> {
    @Override
    public void start(StartContext context) throws StartException {
        String jbossHome = this.jbossHomeInjector.getValue();

        String clientDir = new File(jbossHome, "client").getAbsolutePath();

        System.setProperty("io.liveoak.js.dir", clientDir);
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<String> jbossHomeInjector() {
        return jbossHomeInjector;
    }

    private InjectedValue<String> jbossHomeInjector = new InjectedValue<>();
}
