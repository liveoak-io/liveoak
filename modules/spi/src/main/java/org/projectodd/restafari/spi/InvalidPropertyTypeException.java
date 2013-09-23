package org.projectodd.restafari.spi;

public class InvalidPropertyTypeException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public InvalidPropertyTypeException(String name, Class<?> requestedType) {
        this.name = name;
        this.requestedType = requestedType;
    }
    
    private String name;
    private Class<?> requestedType;

}
