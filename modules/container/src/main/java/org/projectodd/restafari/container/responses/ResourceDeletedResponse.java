package org.projectodd.restafari.container.responses;

import org.projectodd.restafari.spi.Resource;

public class ResourceDeletedResponse extends BaseResourceResponse {

    public ResourceDeletedResponse(String mimeType, Resource resource) {
        super(mimeType, resource);
    }

}
