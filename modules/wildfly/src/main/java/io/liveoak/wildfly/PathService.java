package io.liveoak.wildfly;

import java.io.File;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PathService implements Service<String> {

    public PathService(String name) {
        this.name = name;
    }

    @Override
    public void start(StartContext context) throws StartException {

    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public String getValue() throws IllegalStateException, IllegalArgumentException {
        String parent = this.parentPathInjector.getValue();
        if (parent == null) {
            return new File(name).getAbsolutePath();
        } else {
            return new File(parent, name).getAbsolutePath();
        }
    }

    public Injector<String> parentPathInjector() {
        return this.parentPathInjector;
    }

    private InjectedValue<String> parentPathInjector = new InjectedValue<>();

    private String name;
}
