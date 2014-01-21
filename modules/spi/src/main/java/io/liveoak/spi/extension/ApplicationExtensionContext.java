package io.liveoak.spi.extension;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.spi.Application;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Bob McWhirter
 */
public interface ApplicationExtensionContext {

    String id();
    Application application();
    ServiceTarget target();

    ServiceName configurationServiceName();

    void mountPublic(ServiceName publicName);
    void mountPrivate(ServiceName privateName);

}
