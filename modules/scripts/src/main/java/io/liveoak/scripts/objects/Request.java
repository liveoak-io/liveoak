package io.liveoak.scripts.objects;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface Request {

    String getRequestID();

    String getResourcePath();

    void setResourcePath(String resourcePath);

    String getResourceType();

    Resource getResource();

    void setResource(Resource resource); //TODO

    RequestContext getRequestContext();
}
