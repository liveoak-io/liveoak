package io.liveoak.spi.extension;

import io.liveoak.spi.Application;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public interface ApplicationExtensionContext {

    String extensionId();
    String resourceId();

    Application application();

    ServiceTarget target();

    void mountPublic();
    void mountPublic(ServiceName publicName);
    void mountPublic(ServiceName publicName, MediaType mediaType);
    void mountPublic(ServiceName publicName, MediaType mediaType, boolean makeDefault);
    void mountPublic(RootResource publicResource);
    void mountPublic(RootResource publicResource, MediaType mediaType);
    void mountPublic(RootResource publicResource, MediaType mediaType, boolean makeDefault);

    void mountPrivate();
    void mountPrivate(ServiceName privateName);
    void mountPrivate(ServiceName privateName, MediaType mediaType);
    void mountPrivate(ServiceName privateName, MediaType mediaType, boolean makeDefault);
    void mountPrivate(RootResource privateResource);
    void mountPrivate(RootResource privateResource, MediaType mediaType);
    void mountPrivate(RootResource privateResource, MediaType mediaType, boolean makeDefault);

}
