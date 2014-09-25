package io.liveoak.scripts.objects;

import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public interface Resource {
    String getId();

    void setId(String id);

    String getUri();

    void setUri(String uri) throws Exception;

    Map<String, Object> getProperties();

    void setProperties(Map<String, Object> properties);

    Resource[] getMembers();

    default void setMembers(Object id) throws Exception {
        throw Util.notEditable("members");
    }

}