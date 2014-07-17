package io.liveoak.mongo.internal;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class InternalStorageService implements Service<InternalStorage> {

    InternalStorage internalStorage;

    @Override
    public void start(StartContext context) throws StartException {
        InternalStorageManager internalStorageManager = internalStorageManagerInjector.getValue();
        this.internalStorage = internalStorageManager.getInternalStorage(appNameInjector.getValue(), resourceIdInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public InternalStorage getValue() throws IllegalStateException, IllegalArgumentException {
        return internalStorage;
    }

    public InjectedValue<InternalStorageManager> internalStorageManagerInjector = new InjectedValue<InternalStorageManager>();

    public InjectedValue<String> appNameInjector = new InjectedValue<String>();
    public InjectedValue<String> resourceIdInjector = new InjectedValue<String>();
}

