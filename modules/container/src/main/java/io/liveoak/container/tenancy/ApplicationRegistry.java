package io.liveoak.container.tenancy;

import java.util.Collection;

import io.liveoak.spi.Application;

/**
 * @author Bob McWhirter
 */
public interface ApplicationRegistry {

    Collection<? extends Application> applications();

    Application application(String id) throws InterruptedException;
}
