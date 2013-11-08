package org.projectodd.restafari.container;

import org.projectodd.restafari.container.codec.MediaTypeMatcher;
import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.ResourceParams;
import org.projectodd.restafari.spi.ReturnFields;
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

    private ResourceRequest(RequestType type, ResourcePath path) {
        if (type == null) {
            throw new IllegalArgumentException("requestType is null");
        }
        if (path == null) {
            throw new IllegalArgumentException("resourcePath is null");
        }
        this.requestType = type;
        this.resourcePath = path;
    }

    public RequestType requestType() {
        return this.requestType;
    }

    public ResourcePath resourcePath() {
        return this.resourcePath;
    }

    public MediaTypeMatcher mediaTypeMatcher() {
        return this.mediaTypeMatcher;
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

    public String authorizationToken() {
        return this.authorizationToken;
    }

    public ReturnFields returnFields() {
        return this.returnFields;
    }

    public RequestContext requestContext() {
        return this.requestContext;
    }

    public String toString() {
        return "[ResourceRequest: type=" + this.requestType() + "; path=" + this.resourcePath + "]";
    }

    private RequestType requestType;
    private ResourcePath resourcePath;
    private ResourceParams params;
    private MediaTypeMatcher mediaTypeMatcher;
    private Pagination pagination;
    private ResourceState state;
    private String authorizationToken;
    private ReturnFields returnFields;
    private RequestContext requestContext;

    public static class Builder {

        private ResourceRequest obj;

        public Builder(RequestType type, ResourcePath path) {
            obj = new ResourceRequest(type, path);
        }

        public Builder resourceParams(ResourceParams params) {
            obj.params = params;
            return this;
        }

        public Builder mediaTypeMatcher(MediaTypeMatcher mediaTypeMatcher) {
            obj.mediaTypeMatcher = mediaTypeMatcher;
            return this;
        }

        public Builder pagination(Pagination pagination) {
            obj.pagination = pagination;
            return this;
        }

        public Builder resourceState(ResourceState state) {
            obj.state = state;
            return this;
        }

        public Builder returnFields(ReturnFields fields) {
            obj.returnFields = fields;
            return this;
        }

        public Builder authorizationToken(String authToken) {
            obj.authorizationToken = authToken;
            return this;
        }

        public Builder requestContext(RequestContext requestContext) {
            obj.requestContext = requestContext;
            return this;
        }

        public ResourceRequest build() {
            if (obj.mediaTypeMatcher == null) {
                obj.mediaTypeMatcher = new MediaTypeMatcher("application/json");
            }

            if (obj.requestContext == null) {
                if (obj.pagination == null) {
                    obj.pagination = Pagination.NONE;
                }
                if (obj.params == null) {
                    obj.params = ResourceParams.NONE;
                }
                obj.requestContext = new DefaultRequestContext(null, obj.pagination, obj.returnFields, obj.params);
            }
            return obj;
        }
    }
}
