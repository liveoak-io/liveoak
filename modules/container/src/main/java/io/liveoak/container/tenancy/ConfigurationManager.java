package io.liveoak.container.tenancy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @author Ken Finnigan
 */
public interface ConfigurationManager {

    public void removeResource(String id) throws Exception;

    public void updateResource(String id, String type, JsonNode config) throws Exception;

    public ObjectNode readResource(String id) throws Exception;

}
