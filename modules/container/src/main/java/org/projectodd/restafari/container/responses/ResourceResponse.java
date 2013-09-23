package org.projectodd.restafari.container.responses;

import org.projectodd.restafari.spi.Resource;

public class ResourceResponse extends BaseResourceResponse {

    public ResourceResponse(String mimeType, Resource resource) {
        super(mimeType, resource);
    }

}
