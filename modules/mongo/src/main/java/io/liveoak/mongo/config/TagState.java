package io.liveoak.mongo.config;

import java.util.HashMap;
import java.util.Map;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class TagState extends EmbeddedConfigResource {

    private String name;
    private String value;

    public TagState(Resource parent, String name, String value) {
        super(parent);
        this.name = name;
        this.value = value;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put(name, value);
        return properties;
    }
}
