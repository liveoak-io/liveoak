package io.liveoak.container.tenancy;

import java.util.Collection;

import io.liveoak.spi.Application;

/**
 * @author Bob McWhirter
 */
public interface ApplicationRegistry {

    public Collection<? extends Application> applications();

    public Application application(String id) throws InterruptedException;
}
