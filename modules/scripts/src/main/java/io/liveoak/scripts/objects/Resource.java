package io.liveoak.scripts.objects;

import java.util.List;
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

    List<Resource> getMembers();
}