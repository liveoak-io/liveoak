/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

public class InvalidPropertyTypeException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidPropertyTypeException(String name, Class<?> requestedType) {
        this.name = name;
        this.requestedType = requestedType;
    }

    private String name;
    private Class<?> requestedType;

    @Override
    public String getMessage() {
        return "Invalid property type. The property named '" + name + "' expects a type of " + requestedType.getSimpleName();
    }
}
