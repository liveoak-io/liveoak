package io.liveoak.wildfly;

import java.io.File;

import org.jboss.logging.Logger;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class PathService implements Service<String> {

    private static final Logger log = Logger.getLogger(PathService.class);

    public PathService(String name) {
        this.name = name;
    }

    public PathService(ServiceName serviceName, String name) {
        this.serviceName = serviceName;
        this.name = name;
    }

    @Override
    public void start(StartContext context) throws StartException {
        if (serviceName != null) {
            String propName = serviceName.getSimpleName();
            String oldVal = System.getProperty(propName);
            String newVal = getValue();
            if (oldVal != null && !newVal.equals(oldVal)) {
                log.warn("Overwriting system property '" + propName + "' to: [" + newVal + "] (was: " + oldVal + ")");
            }
            System.setProperty(propName, newVal);
            log.debug("Set system property '" + propName + "' to [" + newVal + "]");
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public String getValue() throws IllegalStateException, IllegalArgumentException {
        String parent = this.parentPathInjector.getValue();
        if (parent == null) {
            return new File(name).getAbsolutePath();
        } else if (name == null) {
            return new File(parent).getAbsolutePath();
        } else {
            return new File(parent, name).getAbsolutePath();
        }
    }

    public Injector<String> parentPathInjector() {
        return this.parentPathInjector;
    }

    private InjectedValue<String> parentPathInjector = new InjectedValue<>();

    private String name;
    private ServiceName serviceName;
}
