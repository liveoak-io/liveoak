package io.liveoak.container.extension;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.resource.RootResource;

/**
 * @author Ken Finnigan
 */
public class MediaTypeMountService<T extends RootResource> extends MountService<T> {

    public MediaTypeMountService() {
    }

    public MediaTypeMountService(String verifyId, MediaType mediaType, boolean makeDefault) {
        super(verifyId);
        this.mediaType = mediaType;
        this.makeDefault = makeDefault;
    }

    protected void mountResource() {
        if (this.mediaType != null) {
            this.mountableInjector.getValue().registerResource(this.resourceInjector.getValue(), this.mediaType, this.makeDefault);
        } else {
            this.mountableInjector.getValue().registerResource(this.resourceInjector.getValue());
        }
    }

    protected void unmountResource() {
        if (this.mediaType != null) {
            this.mountableInjector.getValue().unregisterResource(this.resourceInjector.getValue(), this.mediaType);
        } else {
            this.mountableInjector.getValue().unregisterResource(this.resourceInjector.getValue());
        }
    }

    private MediaType mediaType;
    private boolean makeDefault;
}
