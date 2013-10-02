package org.projectodd.restafari.mongo;

import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.projectodd.restafari.spi.InvalidPropertyTypeException;
import org.projectodd.restafari.spi.ObjectResource;
import org.projectodd.restafari.spi.ResourceReference;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class DBObjectResource implements ObjectResource {
    private static final String ID_FIELD = "_id";

    private final DBObject object;

    public DBObjectResource(DBObject object) {
        this.object = object;
    }

    @Override
    public String getId() {
        Object id = object.get(ID_FIELD);
        return (id == null) ? null : id.toString();
    }

    @Override
    public void setId(String id) {
        if (object.get(ID_FIELD) == null) {
            object.put(ID_FIELD, id);
        } else {
            throw new IllegalStateException("Cannot re-assign an id for id: " + getId());
        }
    }

    @Override
    public ObjectResource setString(String name, String value) {
        return put(name, value);
    }

    @Override
    public String getString(String name) throws InvalidPropertyTypeException {
        return get(name, String.class);
    }

    @Override
    public ObjectResource setInteger(String name, int value) {
        return put(name, value);
    }

    @Override
    public int getInteger(String name) throws InvalidPropertyTypeException {
        return get(name, Integer.class);
    }

    @Override
    public ObjectResource setDouble(String name, double value) {
        return put(name, value);
    }

    @Override
    public double getDouble(String name) throws InvalidPropertyTypeException {
        return get(name, Double.class);
    }

    @Override
    public ObjectResource setResource(String name, ObjectResource value) {
        return put(name, value);
    }

    @Override
    public ObjectResource getResource(String name) throws InvalidPropertyTypeException {
        return new DBObjectResource((DBObject) object.get(name));
    }

    @Override
    public ObjectResource setReference(String name, ResourceReference value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceReference getReference(String name) throws InvalidPropertyTypeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getProperty(String name) {
        return object.get(name);
    }

    @Override
    public Set<String> getPropertyNames() {
        return object.keySet().stream().filter(key -> !key.equals("_id")).collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private <T> T get(String name, Class<T> type) throws InvalidPropertyTypeException {
        Object o = getProperty(name);
        if (o == null) return null;

        if (!type.isInstance(o)) {
            throw new InvalidPropertyTypeException(name, type);
        }

        return (T) o;
    }

    private ObjectResource put(String name, Object value) {
        if (value instanceof DBObjectResource) {
            object.put(name, ((DBObjectResource) value).object);
        } else {
            object.put(name, value);
        }
        return this;
    }
}
