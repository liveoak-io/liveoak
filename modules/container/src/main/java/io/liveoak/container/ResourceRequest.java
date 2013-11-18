/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.container.codec.MediaTypeMatcher;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.SecurityContext;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ResourceRequest {

    private ResourceRequest( RequestType type, ResourcePath path ) {
        if ( type == null ) {
            throw new IllegalArgumentException( "requestType is null" );
        }
        if ( path == null ) {
            throw new IllegalArgumentException( "resourcePath is null" );
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

    public RequestAttributes requestAttributes() {
        return this.requestAttributes;
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
    private RequestAttributes requestAttributes;
    private ResourceParams params;
    private MediaTypeMatcher mediaTypeMatcher;
    private Pagination pagination;
    private ResourceState state;
    private ReturnFields returnFields;
    private RequestContext requestContext;

    public static class Builder {

        private ResourceRequest obj;

        public Builder( RequestType type, ResourcePath path ) {
            obj = new ResourceRequest( type, path );
        }

        public Builder resourceParams( ResourceParams params ) {
            obj.params = params;
            return this;
        }

        public Builder mediaTypeMatcher( MediaTypeMatcher mediaTypeMatcher ) {
            obj.mediaTypeMatcher = mediaTypeMatcher;
            return this;
        }

        public Builder pagination( Pagination pagination ) {
            obj.pagination = pagination;
            return this;
        }

        public Builder resourceState( ResourceState state ) {
            obj.state = state;
            return this;
        }

        public Builder returnFields( ReturnFields fields ) {
            obj.returnFields = fields;
            return this;
        }

        public Builder requestAttributes( RequestAttributes reqAttributes ) {
            obj.requestAttributes = reqAttributes;
            return this;
        }

        public Builder requestAttribute( String attributeName, Object attributeValue ) {
            if ( obj.requestAttributes == null ) {
                obj.requestAttributes = new DefaultRequestAttributes();
            }
            obj.requestAttributes.setAttribute( attributeName, attributeValue );
            return this;
        }

        public Builder requestContext( RequestContext requestContext ) {
            obj.requestContext = requestContext;
            return this;
        }

        public ResourceRequest build() {
            if ( obj.mediaTypeMatcher == null ) {
                obj.mediaTypeMatcher = new MediaTypeMatcher( "application/json" );
            }

            if ( obj.requestContext == null ) {
                if ( obj.pagination == null ) {
                    obj.pagination = Pagination.NONE;
                }
                if ( obj.params == null ) {
                    obj.params = ResourceParams.NONE;
                }
                if ( obj.requestAttributes == null ) {
                    obj.requestAttributes = new DefaultRequestAttributes();
                }
                if ( obj.returnFields == null ) {
                    obj.returnFields = ReturnFields.ALL;
                }

                obj.requestContext = new DefaultRequestContext( SecurityContext.ANONYMOUS, obj.pagination, obj.returnFields, obj.params, obj.resourcePath, obj.requestType, obj.requestAttributes );
            }
            return obj;
        }
    }
}
