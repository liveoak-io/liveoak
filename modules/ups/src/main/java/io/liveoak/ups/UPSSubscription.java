package io.liveoak.ups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class UPSSubscription {
    DBObject dbObject;

    static final String MONGO_ID = "_id";
    static final String RESOURCE_PATH = "resource-path";
    static final String ENABLED = "enabled";

    static final String VARIANTS = "variants";
    static final String ALIASES = "alias";
    static final String CATEGORIES = "categories";
    static final String DEVICES = "device-type";
    static final String SIMPLE_PUSH = "simple-push";
    static final String MESSAGE = "message";

    private UPSSubscription(DBObject dbObject) {
        this.dbObject = dbObject;
    }

    public static UPSSubscription create(DBObject dbObject) {
        Object resourcePath = dbObject.get(RESOURCE_PATH);

        // the resources path property is required. If its not available or not a string
        // then the dbObject we are dealing with is not a valid UPSSubscription and should be ignored.
        if (resourcePath == null || !(resourcePath instanceof String)) {
            return null;
        }
        // if the id property is not set, then we are dealing with an embedded document, ignore it
        Object id = dbObject.get(MONGO_ID);
        if (id == null) {
            return null;
        }

        UPSSubscription subscription = new UPSSubscription(dbObject);

        return subscription;
    }

    public static UPSSubscription create(ResourceState resourceState) {
        Object resourcePath = resourceState.getProperty(RESOURCE_PATH);
        if (resourcePath == null || !(resourcePath instanceof String)) {
            return null;
        }

        DBObject dbObject = new BasicDBObject();
        dbObject.put(RESOURCE_PATH, resourcePath);

        String id = resourceState.id();
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        dbObject.put(MONGO_ID, id);

        Object enabledProperty = resourceState.getProperty(ENABLED);
        if (enabledProperty != null || !(enabledProperty instanceof Boolean)) {
            dbObject.put(ENABLED, true); // default to enabled if its not specified
        } else {
            dbObject.put(ENABLED, (Boolean) enabledProperty);
        }

        Object aliasProperty = resourceState.getProperty(ALIASES);
        if (aliasProperty != null && aliasProperty instanceof List) {
            dbObject.put(ALIASES, aliasProperty);
        }

        Object variantsProperty = resourceState.getProperty(VARIANTS);
        if (variantsProperty != null && variantsProperty instanceof List) {
            dbObject.put(VARIANTS, variantsProperty);
        }

        Object categoriesProperty = resourceState.getProperty(CATEGORIES);
        if (categoriesProperty != null && categoriesProperty instanceof List) {
            dbObject.put(CATEGORIES, categoriesProperty);
        }

        Object devicesProperty = resourceState.getProperty(DEVICES);
        if (devicesProperty != null && devicesProperty instanceof List) {
            dbObject.put(DEVICES, devicesProperty);
        }

        Object simplePushProperty = resourceState.getProperty(SIMPLE_PUSH);
        if (simplePushProperty != null && simplePushProperty instanceof Integer) {
            dbObject.put(SIMPLE_PUSH, (Integer) simplePushProperty);
        }

        Object messageObject = resourceState.getProperty(MESSAGE);
        if (messageObject != null && messageObject instanceof ResourceState) {
            Map<String, Object> messageAttributes = new HashMap<>();
            ResourceState messageState = (ResourceState) messageObject;
            for (String propertyName : messageState.getPropertyNames()) {
                messageAttributes.put(propertyName, messageState.getProperty(propertyName));
            }
            DBObject attributes = new BasicDBObject();
            attributes.putAll(messageAttributes);
            dbObject.put(MESSAGE, attributes);
        }

        UPSSubscription subscription = new UPSSubscription(dbObject);

        return subscription;
    }

    public DBObject dbObject() {
        return this.dbObject;
    }

    public String id() {
        return (String) dbObject.get(MONGO_ID);
    }

    public ResourcePath resourcePath() {
        String path = (String) dbObject.get(RESOURCE_PATH);
        return new ResourcePath(path);
    }

    public Boolean enabled() {
        Object property = dbObject.get(ENABLED);
        if (property != null && property instanceof Boolean) {
            return (Boolean) property;
        }
        return null;
    }

    public void aliases(List<String> aliases) {
        dbObject.put(ALIASES, aliases);
    }

    public List<String> aliases() {
        return getList(ALIASES);
    }

    public void categories(Set<String> categories) {
        dbObject.put(CATEGORIES, categories);
    }

    public Set<String> categories() {
        Object property = dbObject.get(CATEGORIES);
        if (property != null && property instanceof List) {
            Set<String> set = new HashSet((List) property);
            return set;
        }

        return null;
    }

    public void simplePush(Integer simplePush) {
        dbObject.put(SIMPLE_PUSH, simplePush);
    }

    public Integer simplePush() {
        Object property = dbObject.get(SIMPLE_PUSH);
        if (property != null && property instanceof Integer) {
            return (Integer) property;
        }

        return null;
    }

    public void deviceTypes(List<String> deviceTypes) {
        dbObject.put(DEVICES, deviceTypes);
    }

    public List<String> deviceTypes() {
        return getList(DEVICES);
    }

    public void variants(List<String> variants) {
        dbObject.put(VARIANTS, variants);
    }

    public List<String> variants() {
        return getList(VARIANTS);
    }

    public void setMessage(Map<String, Object> message) {

        DBObject messageAttributes = new BasicDBObject();
        messageAttributes.putAll(message);

        dbObject.put(MESSAGE, messageAttributes);
    }

    public Map<String, Object> message() {
        Map<String, Object> map = new HashMap<>();

        Object property = dbObject.get(MESSAGE);
        if (property == null || property instanceof DBObject) {
            DBObject messageObject = (DBObject) property;
            if (messageObject != null) {
                for (String key : messageObject.keySet()) {
                    map.put(key, messageObject.get(key));
                }
            }
        }

        return map;
    }

    private List<String> getList(String type) {
        Object property = dbObject.get(type);
        if (property != null && property instanceof List) {

            List<String> list = new ArrayList<>();
            for (Object object : (List) property) {
                if (object instanceof String) {
                    list.add((String) object);
                }
            }
            return list;
        }

        return null;
    }
}
