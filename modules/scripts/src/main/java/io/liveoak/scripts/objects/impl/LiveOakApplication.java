package io.liveoak.scripts.objects.impl;

import io.liveoak.scripts.objects.Application;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakApplication implements Application {
    io.liveoak.spi.Application application;

    public LiveOakApplication(io.liveoak.spi.Application application) {
        this.application = application;
    }

    @Override
    public String getId() {
        return application.id();
    }

    @Override
    public String getName() {
        return application.name();
    }

    @Override
    public String getDirectory() {
        return application.directory().toString();
    }

    @Override
    public Boolean getVisible() {
        return application.visible();
    }
}
