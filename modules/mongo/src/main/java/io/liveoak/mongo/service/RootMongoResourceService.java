package io.liveoak.mongo.service;

import com.mongodb.DB;
import io.liveoak.mongo.RootMongoResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class RootMongoResourceService implements Service<RootMongoResource> {

    public RootMongoResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.resource = new RootMongoResource( this.id, dbInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
        this.resource = null;
    }

    @Override
    public RootMongoResource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.resource;
    }

    public Injector<DB> dbInjector() {
        return this.dbInjector;
    }

    private String id;
    private InjectedValue<DB> dbInjector = new InjectedValue<>();
    private RootMongoResource resource;
}
