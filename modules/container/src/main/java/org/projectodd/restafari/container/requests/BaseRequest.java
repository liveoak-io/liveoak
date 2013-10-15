package org.projectodd.restafari.container.requests;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;
import org.projectodd.restafari.container.ResourcePath;
import org.projectodd.restafari.spi.Pagination;

/**
 * @author Bob McWhirter
 */
public class BaseRequest {

    public BaseRequest(ResourcePath resourcePath, String mimeType) {
        this.resourcePath = resourcePath;
        this.mimeType = mimeType;
    }

    public ResourcePath resourcePath(){
        return this.resourcePath;
    }

    public String mimeType() {
        return this.mimeType;
    }

    public boolean isResourceRequest() {
        return this.resourcePath.isResourcePath();
    }

    public boolean isCollectionRequest() {
        return this.resourcePath.isCollectionPath();
    }

    public String type() {
        return this.resourcePath.getType();
    }

    public String collectionName() {
        return this.resourcePath.getCollectionName();
    }

    public String resourceId() {
        return this.resourcePath.getResourceId();
    }

    private ResourcePath resourcePath;
    private String mimeType;
}
