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

    public ResourceRequest(RequestType requestType, ResourcePath resourcePath, ResourceParams params, String mimeType) {
        this.requestType = requestType;
        this.resourcePath = resourcePath;
        this.mimeType = mimeType;
        this.params = params;
        this.pagination = Pagination.NONE;
    }

    public ResourceRequest(RequestType requestType, ResourcePath resourcePath, ResourceParams params, String mimeType, Pagination pagination) {
        this.requestType = requestType;
        this.resourcePath = resourcePath;
        this.mimeType = mimeType;
        this.params = params;
        this.pagination = pagination != null ? pagination : Pagination.NONE;
    }

    public ResourceRequest(RequestType requestType, ResourcePath resourcePath, ResourceParams params, String mimeType, ResourceState state) {
        this.requestType = requestType;
        this.resourcePath = resourcePath;
        this.mimeType = mimeType;
        this.params = params;
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

    public ResourceParams params() {
        return this.params;
    }

    public String toString() {
        return "[ResourceRequest: type=" + this.requestType() + "; path=" + this.resourcePath + "]";
    }

    private RequestType requestType;
    private ResourcePath resourcePath;
    private ResourceParams params;
    private String mimeType;
    private Pagination pagination;
    private ResourceState state;
}
