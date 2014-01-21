package io.liveoak.mongo.gridfs.service;

import com.mongodb.DB;
import io.liveoak.mongo.gridfs.GridFSResource;
import io.liveoak.mongo.gridfs.GridFSRootResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.vertx.java.core.Vertx;

import java.io.File;

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
                this.dbInjector.getValue(),
                this.tmpDirInjector.getValue(),
                this.vertxInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public GridFSRootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<DB> dbInjector() {
        return this.dbInjector;
    }

    public Injector<File> tmpDirInjector() {
        return this.tmpDirInjector;
    }

    public Injector<Vertx> vertxInjector() {
        return this.vertxInjector;
    }

    private final String id;

    private InjectedValue<DB> dbInjector = new InjectedValue<>();
    private InjectedValue<File> tmpDirInjector = new InjectedValue<>();
    private InjectedValue<Vertx> vertxInjector = new InjectedValue<>();
    private GridFSRootResource resource;
}
