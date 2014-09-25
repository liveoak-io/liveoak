package io.liveoak.scripts.objects;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface Response {

    String getType();

    Resource getResource();

    Request getRequest();

    default void setType(Object type) throws Exception {
        throw Util.notEditable("type");
    }

    default void setResource(Object resource) throws Exception {
        throw Util.notEditable("resource");
    }

    default void setRequest(Object request) throws Exception {
        throw Util.notEditable("request");
    }
}
