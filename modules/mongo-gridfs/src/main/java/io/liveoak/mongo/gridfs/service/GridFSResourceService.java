package io.liveoak.mongo.gridfs.service;

import java.io.File;

import io.liveoak.mongo.config.RootMongoConfigResource;
import io.liveoak.mongo.gridfs.GridFSRootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public class GridFSResourceService implements Service<GridFSRootResource> {


    public GridFSResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new GridFSRootResource(
                this.id,
                this.configResourceInjector.getValue(),
                this.tmpDirInjector.getValue(),
                this.vertxInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public GridFSRootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public InjectedValue<RootMongoConfigResource> configResourceInjector() {
        return this.configResourceInjector;
    }

    public Injector<File> tmpDirInjector() {
        return this.tmpDirInjector;
    }

    public Injector<Vertx> vertxInjector() {
        return this.vertxInjector;
    }

    private final String id;

    private InjectedValue<RootMongoConfigResource> configResourceInjector = new InjectedValue<>();
    private InjectedValue<File> tmpDirInjector = new InjectedValue<>();
    private InjectedValue<Vertx> vertxInjector = new InjectedValue<>();
    private GridFSRootResource resource;
}
