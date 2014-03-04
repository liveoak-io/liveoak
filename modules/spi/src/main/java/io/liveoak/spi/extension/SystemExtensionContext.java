package io.liveoak.spi.extension;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Bob McWhirter
 */
public interface SystemExtensionContext {

    String id();
    ServiceTarget target();
    void mountPrivate(ServiceName adminResourceName);
    void mountPrivate(RootResource resource);

    //void mount(ServiceName name);
}
