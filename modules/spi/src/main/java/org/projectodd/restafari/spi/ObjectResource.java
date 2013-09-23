package org.projectodd.restafari.spi;

import java.util.Collection;

public interface ObjectResource extends Resource {
    
    String getId();
    void setId(String id);
    
    void setStringProperty(String name, String value);
    String getStringProperty(String name) throws InvalidPropertyTypeException;
    
    void setIntegerProperty(String name, int value);
    int getIntegerProperty(String name) throws InvalidPropertyTypeException;
    
    void setDoubleProperty(String name, double value);
    double getDoubleProperty(String name) throws InvalidPropertyTypeException;
    
    void setResourceProperty(String name, ObjectResource value);
    ObjectResource getResourceProperty(String name) throws InvalidPropertyTypeException;
    
    void setReferenceProperty(String name, ResourceReference value);
    ResourceReference getReferenceProperty(String name) throws InvalidPropertyTypeException;
    
    Object getProperty(String name);
    Collection<String> getPropertyNames();

}
