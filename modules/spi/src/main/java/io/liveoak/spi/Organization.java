package io.liveoak.spi;

import java.util.Collection;

/**
 * @author Bob McWhirter
 */
public interface Organization {

    String id();
    String name();
    Collection<? extends Application> applications();
    Application application(String id);
}
