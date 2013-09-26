package org.projectodd.restafari.container;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
    public void setStringProperty(String name, String value) {
        checkPropertyName( name );
        this.properties.put( name, value );
    }

    @Override
    public String getStringProperty(String name) throws InvalidPropertyTypeException {
        return getProperty(name, String.class);
    }

    @Override
    public void setIntegerProperty(String name, int value) {
        checkPropertyName( name );
        this.properties.put(name, value);
    }

    @Override
    public int getIntegerProperty(String name) throws InvalidPropertyTypeException {
        return getProperty(name, Integer.class);
    }

    @Override
    public void setDoubleProperty(String name, double value) {
        checkPropertyName( name );
        this.properties.put( name, value );
    }

    @Override
    public double getDoubleProperty(String name) throws InvalidPropertyTypeException {
        return getProperty(name, Double.class);
    }

    @Override
    public void setResourceProperty(String name, ObjectResource value) {
        checkPropertyName( name );
        this.properties.put( name, value );
    }

    @Override
    public ObjectResource getResourceProperty(String name) throws InvalidPropertyTypeException {
        return getProperty( name, ObjectResource.class );
    }

    @Override
    public void setReferenceProperty(String name, ResourceReference value) {
        checkPropertyName( name );
        this.properties.put( name, value );
    }

    @Override
    public ResourceReference getReferenceProperty(String name) throws InvalidPropertyTypeException {
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
    public Collection<String> getPropertyNames() {
        return this.properties.keySet();
    }

    public String toString() {
        return this.properties.toString();

    }

    private String id;
    private Map<String, Object> properties = new HashMap<>();

}