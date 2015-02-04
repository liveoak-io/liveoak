package io.liveoak.mongo.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import com.mongodb.TaggableReadPreference;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.InitializationException;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ReadPreferenceState extends EmbeddedConfigResource {

    public static final String ID = "read-preference";
    
    // property names
    public static final String TAGS = "tags";
    public static final String TYPE = "type";

    // read preference values
    public static final String PRIMARY = "primary";
    public static final String PRIMARY_PREFERRED = "primaryPreferred";
    public static final String SECONDARY = "secondary";
    public static final String SECONDARY_PREFERRED = "secondaryPreferred";
    public static final String NEAREST = "nearest";

    ReadPreference readPreference;

    public ReadPreferenceState(Resource parent,ReadPreference readPreference) {
        super(parent);
        this.readPreference = readPreference;
    }

    public ReadPreferenceState(Resource parent, ResourceState state) throws Exception {
        super(parent);
        String type = state.getProperty(TYPE, false, String.class);
        // If the type is null, default to the primary type
        if (type == null) {
            type = PRIMARY;
        }

        ResourceState tagObject = state.getProperty(TAGS, false, ResourceState.class);
        DBObject tags = new BasicDBObject();
        if (tagObject != null) {
            for (String name : tagObject.getPropertyNames()) {
                tags.put(name, tagObject.getProperty(name, true, String.class));
            }
        }

        ReadPreference readPreference;
        switch (type) {
            case PRIMARY:
                readPreference = ReadPreference.primary();
                break;
            case PRIMARY_PREFERRED:
                if (tagObject != null) {
                    readPreference = ReadPreference.primaryPreferred(tags);
                } else {
                    readPreference = ReadPreference.primaryPreferred();
                }
                break;
            case SECONDARY: {
                if (tagObject != null) {
                    readPreference = ReadPreference.secondary(tags);
                } else {
                    readPreference = ReadPreference.secondary();
                }
                break;
            }
            case SECONDARY_PREFERRED:
                if (tagObject != null) {
                    readPreference = ReadPreference.secondaryPreferred(tags);
                } else {
                    readPreference = ReadPreference.secondaryPreferred();
                }
                break;
            case NEAREST:
                if (tagObject != null) {
                    readPreference = ReadPreference.nearest(tags);
                } else {
                    readPreference = ReadPreference.nearest();
                }
                break;
            default:
                throw new InitializationException("Unknown read preference type : [" + type + "]");

        }

        this.readPreference = readPreference;
    }

    public ReadPreference getReadPreference() {
        return readPreference;
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map properties = new HashMap();
        properties.put(TYPE, readPreference.getName());

        if (this.readPreference instanceof TaggableReadPreference) {
            TaggableReadPreference taggedReadPreference = (TaggableReadPreference) this.readPreference;
            List<DBObject> tags = taggedReadPreference.getTagSets();

            List<Resource> tagList = new ArrayList<>();
            for (DBObject tag : tags) {
                for (String key : tag.keySet()) {
                    tagList.add(new TagState(this, key, (String) tag.get(key)));
                }
            }
            properties.put(TAGS, tagList);
        }

        return properties;
    }
}
