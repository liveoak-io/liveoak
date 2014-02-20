package io.liveoak.container.tenancy;

import io.liveoak.spi.Application;

import java.io.File;
import java.util.Collection;

/**
 * @author Bob McWhirter
 */
public interface ApplicationRegistry {

    public Collection<? extends Application> applications();
    public Application application(String id) throws InterruptedException;
}
