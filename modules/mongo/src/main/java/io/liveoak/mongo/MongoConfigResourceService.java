package io.liveoak.mongo;

import io.liveoak.mongo.config.MongoDatastoresRegistry;
import io.liveoak.mongo.config.RootMongoConfigResource;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoConfigResourceService implements Service<RootMongoConfigResource> {

    private RootMongoConfigResource mongoConfigRootResource;
    private String id;

    public MongoConfigResourceService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.mongoConfigRootResource = new RootMongoConfigResource(id, mongoDatastoreInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        mongoConfigRootResource = null;
    }

    @Override
    public RootMongoConfigResource getValue() throws IllegalStateException, IllegalArgumentException {
        return mongoConfigRootResource;
    }

    public InjectedValue<MongoDatastoresRegistry> mongoDatastoreInjector = new InjectedValue<>();
}