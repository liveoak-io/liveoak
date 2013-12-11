package io.liveoak.container.service;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.PlatformManager;

/**
 * @author Bob McWhirter
 */
public class VertxService implements Service<Vertx> {

    public VertxService() {

    }

    @Override
    public void start(StartContext context) throws StartException {
        this.vertx = this.platformManagerInjector.getValue().vertx();
    }

    @Override
    public void stop(StopContext context) {
        this.vertx = null;
    }

    @Override
    public Vertx getValue() throws IllegalStateException, IllegalArgumentException {
        return this.vertx;
    }

    public Injector<PlatformManager> platformManagerInjector() {
        return this.platformManagerInjector;
    }

    private InjectedValue<PlatformManager> platformManagerInjector = new InjectedValue<>();
    private Vertx vertx;
}
