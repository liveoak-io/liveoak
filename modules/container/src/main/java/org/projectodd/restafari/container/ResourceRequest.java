package org.projectodd.restafari.container;

import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ResourceRequest {

    public enum RequestType {
        CREATE,
        READ,
        UPDATE,
        DELETE,
    }

    public ResourceRequest(RequestType requestType, ResourcePath resourcePath, String mimeType) {
        this.requestType = requestType;
        this.resourcePath = resourcePath;
        this.mimeType = mimeType;
        this.pagination = Pagination.NONE;
    }

    public ResourceRequest(RequestType requestType, ResourcePath resourcePath, String mimeType, Pagination pagination) {
        this.requestType = requestType;
        this.resourcePath = resourcePath;
        this.mimeType = mimeType;
        this.pagination = pagination;
    }

    public ResourceRequest(RequestType requestType, ResourcePath resourcePath, String mimeType, ResourceState state) {
        this.requestType = requestType;
        this.resourcePath = resourcePath;
        this.mimeType = mimeType;
        this.state = state;
        this.pagination = Pagination.NONE;
    }

    public RequestType requestType() {
        return this.requestType;
    }

    public ResourcePath resourcePath() {
        return this.resourcePath;
    }

    public String mimeType() {
        return this.mimeType;
    }

    public ResourceState state() {
        return this.state;
    }

    public Pagination pagination() {
        return this.pagination;
    }

    private RequestType requestType;
    private ResourcePath resourcePath;
    private String mimeType;
    private Pagination pagination;
    private ResourceState state;
}
