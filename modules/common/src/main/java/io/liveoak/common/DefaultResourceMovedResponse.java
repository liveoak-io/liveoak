package io.liveoak.common;

import java.net.URI;

import io.liveoak.spi.ResourceMovedResponse;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceRequest;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class DefaultResourceMovedResponse extends DefaultResourceResponse implements ResourceMovedResponse {

    protected URI redirectURI;
    protected ResourcePath resourcePath;
    protected MovedType movedType;
    protected Integer maxAge;

    public DefaultResourceMovedResponse(ResourceRequest inReplyTo, MovedType movedType, ResourcePath resourcePath) {
        super(inReplyTo, ResponseType.MOVED);
        this.movedType = movedType;
        this.resourcePath = resourcePath;
    }

    public DefaultResourceMovedResponse(ResourceRequest inReplyTo, MovedType movedType, URI redirectURI) {
        super(inReplyTo, ResponseType.MOVED);
        this.movedType = movedType;
        this.redirectURI = redirectURI;
    }

    @Override
    public String redirectURL() {
        if (resourcePath != null) {
            return resourcePath.toString();
        } else {
            return redirectURI.toString();
        }
    }

    @Override
    public MovedType movedType() {
        return movedType;
    }

    public void maxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    @Override
    public Integer maxAge() {
        return maxAge;
    }
}
