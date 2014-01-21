package io.liveoak.container.tenancy.service;

import io.liveoak.container.extension.CommonExtensions;
import io.liveoak.container.tenancy.InternalApplication;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class CommonApplicationExtensionsService implements Service<Void> {
    @Override
    public void start(StartContext context) throws StartException {
        CommonExtensions commonExtensions = this.commonExtensionsInjector.getOptionalValue();
        if (commonExtensions != null) {
            commonExtensions.extend(this.applicationInjector.getValue());
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<InternalApplication> applicationInjector() {
        return this.applicationInjector;
    }

    public Injector<CommonExtensions> commonExtensionsInjector() {
        return this.commonExtensionsInjector;
    }

    private InjectedValue<InternalApplication> applicationInjector = new InjectedValue<>();
    private InjectedValue<CommonExtensions> commonExtensionsInjector = new InjectedValue<>();
}
