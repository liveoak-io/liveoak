package org.projectodd.restafari.container.responses;

import java.util.Collection;

import org.projectodd.restafari.spi.Resource;

public class ResourcesResponse extends BaseResponse {

    public ResourcesResponse(String mimeType, Collection<Resource> resources) {
        super(mimeType);
        this.resources = resources;
    }
    
    public Collection<Resource> getResources() {
        return this.resources;
    }

    private Collection<Resource> resources;

}
