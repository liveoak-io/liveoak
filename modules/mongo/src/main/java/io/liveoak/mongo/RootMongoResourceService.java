package io.liveoak.mongo;

import io.liveoak.mongo.config.RootMongoConfigResource;
import io.liveoak.mongo.config.MongoSystemConfigResource;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RootMongoResourceService implements Service<RootMongoResource> {

    private RootMongoResource rootMongoResource;
    private String id;

    public RootMongoResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.rootMongoResource = new RootMongoResource(id, mongoSystemConfigInjector.getValue(), mongoConfigInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        rootMongoResource = null;
    }

    @Override
    public RootMongoResource getValue() throws IllegalStateException, IllegalArgumentException {
        return rootMongoResource;
    }

    public InjectedValue<MongoSystemConfigResource> mongoSystemConfigInjector = new InjectedValue<>();
    public InjectedValue<RootMongoConfigResource> mongoConfigInjector = new InjectedValue<>();
}