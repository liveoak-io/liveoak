package org.projectodd.restafari.container;

import org.projectodd.restafari.container.codec.MediaTypeMatcher;
import org.projectodd.restafari.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class ResourceResponse {

    public enum ResponseType {
        CREATED,
        READ,
        UPDATED,
        DELETED,
        ERROR,
    }

    public ResourceResponse(ResourceRequest inReplyTo, ResponseType responseType) {
        this.inReplyTo = inReplyTo;
        this.responseType = responseType;
    }

    public ResourceResponse(ResourceRequest inReplyTo, ResponseType responseType, Resource resource) {
        this.inReplyTo = inReplyTo;
        this.responseType = responseType;
        this.resource = resource;
    }

    public MediaTypeMatcher mediaTypeMatcher() {
        return this.inReplyTo.mediaTypeMatcher();
    }

    public ResourceRequest inReplyTo() {
        return this.inReplyTo;
    }

    public ResponseType responseType() {
        return this.responseType;
    }

    public Resource resource() {
        return this.resource;
    }

    public String toString() {
        return "[ResourceResponse: type=" + this.responseType + "; object=" + this.resource + "]";
    }

    private ResourceRequest inReplyTo;
    private ResponseType responseType;
    private Resource resource;
}
