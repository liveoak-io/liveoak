package io.liveoak.scripts.objects;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface LocalClient {

    Resource create(String path, Resource resource) throws Exception;

    Resource read(String path) throws Exception;

    Resource update(String path, Resource resource) throws Exception;

    Resource delete(String path) throws Exception;

    Resource create(String path, Resource resource, RequestContext requestContext);

    Resource read(String path, RequestContext requestContext);

    Resource update(String path, Resource resource, RequestContext requestContext);

    Resource delete(String path, RequestContext requestContext);

}
