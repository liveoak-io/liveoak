package org.projectodd.restafari.container.responses;

import org.projectodd.restafari.spi.Resource;

public class ResourceUpdatedResponse extends BaseResourceResponse {

    public ResourceUpdatedResponse(String mimeType, Resource resource) {
        super(mimeType, resource);
    }

}
