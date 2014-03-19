package io.liveoak.mongo.config;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import com.mongodb.TaggableReadPreference;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ReadPreferenceResource implements Resource {

    RootMongoConfigResource parent;
    ReadPreference readPreference;
    public static final String ID = "ReadPreference";

    public enum Options {
        TYPE("type"),
        TAGS("tags");

        private final String propertyName;
        Options(String propertyName){
            this.propertyName = propertyName;
        }

        public String toString() {
            return propertyName;
        }
    }

    public enum Types {
        PRIMARY("primary"),
        PRIMARY_PREFERRED("primaryPreferred"),
        SECONDARY("secondary"),
        SECONDARY_PREFERRED("secondaryPreferred"),
        NEAREST("nearest");

        private final String propertyName;
        Types(String propertyName){
            this.propertyName = propertyName;
        }

        public String toString() {
            return propertyName;
        }
    }

    public ReadPreferenceResource(RootMongoConfigResource parent, ReadPreference readPreference) {
        this.parent = parent;
        this.readPreference = readPreference;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(Options.TYPE.toString(), this.readPreference.getName());

        if (this.readPreference instanceof TaggableReadPreference )
        {
            TaggableReadPreference taggedReadPreference = (TaggableReadPreference) this.readPreference;
            List<DBObject> tags = taggedReadPreference.getTagSets();

            List<TagResource> tagResources = new ArrayList<TagResource>();
            for (DBObject tag : tags) {
                for (String key : tag.keySet()) {
                    TagResource tagResource = new TagResource(this, key, tag.get(key));
                    tagResources.add(tagResource);
                }
            }
            sink.accept( Options.TAGS.toString(), tagResources );
        }

        sink.close();
    }

    public void updateReadPreference(ResourceState state) throws Exception {
        String name = (String)state.getProperty(Options.TYPE.toString());
        if (name == null) {
            name = this.readPreference.getName();
        }

        Object tagsObject = state.getProperty( Options.TAGS.toString() );
        if (tagsObject != null) {
            ResourceState tags = (ResourceState) tagsObject;
            DBObject dbObjectTag = new BasicDBObject();
            Set<String> propertyNames = tags.getPropertyNames();
            for (String propertyName: propertyNames) {
                dbObjectTag.put(propertyName, tags.getProperty( propertyName ));
            }

            if ( name.equals(Types.PRIMARY_PREFERRED.toString()) ) {
                this.readPreference = ReadPreference.primaryPreferred(dbObjectTag);
            } else if ( name.equals(Types.SECONDARY.toString()) ) {
                this.readPreference = ReadPreference.secondary(dbObjectTag);
            } else if ( name.equals(Types.SECONDARY_PREFERRED.toString()) ) {
                this.readPreference = ReadPreference.secondaryPreferred(dbObjectTag);
            } else if ( name.equals(Types.NEAREST.toString()) ) {
                this.readPreference = ReadPreference.nearest(dbObjectTag);
            } else {
                throw new InitializationException("Unknown read preference type : [" + name  + "]");
            }
        } else {

            if ( name.equals(Types.PRIMARY.toString()) ) {
                this.readPreference = ReadPreference.primary();
            } else if ( name.equals(Types.PRIMARY_PREFERRED.toString())) {
                this.readPreference = ReadPreference.primaryPreferred();
            } else if ( name.equals(Types.SECONDARY.toString())) {
                this.readPreference = ReadPreference.secondary();
            } else if ( name.equals(Types.SECONDARY_PREFERRED.toString())) {
                this.readPreference=  ReadPreference.secondaryPreferred();
            } else if ( name.equals(Types.NEAREST.toString()) ) {
                this.readPreference =  ReadPreference.nearest();
            } else {
                throw new InitializationException("Unknown read preference type : [" + name  + "]");
            }
        }

        parent.getMongoClient().setReadPreference( this.readPreference );
    }


    @Override
    public Resource parent() {
        return null;
    }

    @Override
    public String id() {
        return null;
    }
}
