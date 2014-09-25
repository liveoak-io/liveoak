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

    default void setId(Object id) throws Exception {
        throw Util.notEditable("id");
    }

    default void setPath(Object path) throws Exception {
        throw Util.notEditable("path");
    }

    default void setType(Object type) throws Exception {
        throw Util.notEditable("type");
    }

    default void setResource(Object resource) throws Exception {
        throw Util.notEditable("resource");
    }

}
