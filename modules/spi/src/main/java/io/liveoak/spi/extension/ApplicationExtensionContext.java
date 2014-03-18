package io.liveoak.spi.extension;

import io.liveoak.spi.Application;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Bob McWhirter
 */
public interface ApplicationExtensionContext {

    String extensionId();
    String resourceId();

    Application application();

    ServiceTarget target();

    void mountPublic();
    void mountPublic(ServiceName publicName);
    void mountPublic(RootResource publicResource);

    void mountPrivate();
    void mountPrivate(ServiceName privateName);
    void mountPrivate(RootResource privateResource);

}
