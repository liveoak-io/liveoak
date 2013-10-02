package org.projectodd.restafari.container;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.projectodd.restafari.spi.InvalidPropertyTypeException;
import org.projectodd.restafari.spi.ObjectResource;
import org.projectodd.restafari.spi.ResourceReference;

public class SimpleObjectResource implements ObjectResource {

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public ObjectResource setString(String name, String value) {
        checkPropertyName( name );
        this.properties.put( name, value );
        return this;
    }

    @Override
    public String getString(String name) throws InvalidPropertyTypeException {
        return getProperty(name, String.class);
    }

    @Override
    public ObjectResource setInteger(String name, int value) {
        checkPropertyName( name );
        this.properties.put(name, value);
        return this;
    }

    @Override
    public int getInteger(String name) throws InvalidPropertyTypeException {
        return getProperty(name, Integer.class);
    }

    @Override
    public ObjectResource setDouble(String name, double value) {
        checkPropertyName( name );
        this.properties.put( name, value );
        return this;
    }

    @Override
    public double getDouble(String name) throws InvalidPropertyTypeException {
        return getProperty(name, Double.class);
    }

    @Override
    public ObjectResource setResource(String name, ObjectResource value) {
        checkPropertyName( name );
        this.properties.put( name, value );
        return this;
    }

    @Override
    public ObjectResource getResource(String name) throws InvalidPropertyTypeException {
        return getProperty( name, ObjectResource.class );
    }

    @Override
    public ObjectResource setReference(String name, ResourceReference value) {
        checkPropertyName( name );
        this.properties.put( name, value );
        return this;
    }

    @Override
    public ResourceReference getReference(String name) throws InvalidPropertyTypeException {
        return getProperty(name, ResourceReference.class );
    }

    @Override
    public Object getProperty(String name) {
        return this.properties.get(name);
    }

    public void setProperty(String name, Object value) {
        this.properties.put( name, value );
    }

    public <T> T getProperty(String name, Class<T> expectedType) throws InvalidPropertyTypeException {
        Object o = getProperty(name);
        if (o == null) {
            return null;
        }

        if (!expectedType.isInstance(o)) {
            throw new InvalidPropertyTypeException(name, expectedType);
        }

        return (T) o;
    }
    
    protected void checkPropertyName(String name) {
        
    }

    @Override
    public Set<String> getPropertyNames() {
        return this.properties.keySet();
    }

    public String toString() {
        return this.properties.toString();

    }

    private String id;
    private Map<String, Object> properties = new HashMap<>();

}
