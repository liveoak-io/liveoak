package io.liveoak.mongo.config;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDatastoreService implements Service<MongoDatastoreResource> {

    MongoSystemDatastoreResource resource;

    @Override
    public void start(StartContext context) throws StartException {
        resource = new MongoSystemDatastoreResource(idInjector.getValue());

        mongoDatastoreInjector.getValue().addDataStore(idInjector.getValue(), resource);
    }

    @Override
    public void stop(StopContext context) {
       resource = null;
    }

    @Override
    public MongoDatastoreResource getValue() throws IllegalStateException, IllegalArgumentException {
        return resource;
    }

    public InjectedValue<MongoDatastoresRegistry> mongoDatastoreInjector = new InjectedValue<>();
    public InjectedValue<String> idInjector = new InjectedValue<>();
}
