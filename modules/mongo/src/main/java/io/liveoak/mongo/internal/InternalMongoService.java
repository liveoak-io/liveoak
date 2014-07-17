package io.liveoak.mongo.internal;

import java.net.UnknownHostException;

import io.liveoak.mongo.config.RootMongoConfigResource;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class InternalMongoService implements Service<InternalStorageManager> {

    private InternalStorageManager internalStorageManager;

    @Override
    public void start(StartContext context) throws StartException {
        try {
            this.internalStorageManager = new InternalStorageManager(configResourceInjector.getValue());
        } catch (UnknownHostException uhe) {
            throw new StartException("Error starting the InternalStorageManager", uhe);
        }
    }

    @Override
    public void stop(StopContext context) {
        // do nothing for now.
    }

    @Override
    public InternalStorageManager getValue() throws IllegalStateException, IllegalArgumentException {
        return internalStorageManager;
    }


    public InjectedValue<RootMongoConfigResource> configResourceInjector = new InjectedValue<RootMongoConfigResource>();
}
