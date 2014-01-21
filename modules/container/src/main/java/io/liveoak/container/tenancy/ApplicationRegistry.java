package io.liveoak.container.tenancy;

import java.util.Collection;

/**
 * @author Bob McWhirter
 */
public interface ApplicationRegistry {

    Collection<InternalApplication> applications();
    InternalApplication application(String id);
}
