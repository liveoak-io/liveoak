package io.liveoak.scripts.objects;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface Request {

    String getId();

    String getPath();

    String getType();

    Resource getResource();

    RequestContext getContext();
}
