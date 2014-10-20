package io.liveoak.spi.extension;

import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Bob McWhirter
 */
public interface SystemExtensionContext {

    String id();
    String moduleId();
    ServiceTarget target();
    void mountPrivate(ServiceName adminResourceName);
    void mountPrivate(RootResource resource);

    void mountInstance(ServiceName instanceName);
    void mountInstance(RootResource resource);

    //void mount(ServiceName name);
}
