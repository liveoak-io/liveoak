/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.exceptions;

public class InvalidPropertyTypeException extends PropertyException {

    private static final long serialVersionUID = 1L;

    public InvalidPropertyTypeException(String name, Class<?> requestedType) {
        this(name, requestedType, false);
    }

    public InvalidPropertyTypeException(String name, Class<?> requestType, boolean collection) {
        this.name = name;
        this.requestedType = requestType;
        this.collection = collection;
    }

    public InvalidPropertyTypeException(String message) {
        this.message = message;
    }

    protected String name;
    protected Class<?> requestedType;
    protected boolean collection;
    protected String message;

    @Override
    public String getMessage() {
        if (message != null) {
            return message;
        } else {
            if (!collection) {
                return "Invalid property type. The property named '" + name + "' expects a type of " + requestedType.getSimpleName();
            } else {
                return "Invalid property type. The property name '" + name + "' expects a collection of type " + requestedType.getSimpleName();
            }
        }
    }
}
