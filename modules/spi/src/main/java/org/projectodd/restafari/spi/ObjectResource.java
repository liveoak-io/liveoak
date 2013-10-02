package org.projectodd.restafari.spi;

import java.util.Set;

public interface ObjectResource extends Resource {
    
    String getId();
    void setId(String id);
    
    ObjectResource setString(String name, String value);
    String getString(String name) throws InvalidPropertyTypeException;
    
    ObjectResource setInteger(String name, int value);
    int getInteger(String name) throws InvalidPropertyTypeException;
    
    ObjectResource setDouble(String name, double value);
    double getDouble(String name) throws InvalidPropertyTypeException;
    
    ObjectResource setResource(String name, ObjectResource value);
    ObjectResource getResource(String name) throws InvalidPropertyTypeException;
    
    ObjectResource setReference(String name, ResourceReference value);
    ResourceReference getReference(String name) throws InvalidPropertyTypeException;
    
    Object getProperty(String name);
    Set<String> getPropertyNames();

}
