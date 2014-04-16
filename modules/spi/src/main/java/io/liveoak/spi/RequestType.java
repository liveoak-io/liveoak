/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

/**
 * Type of request
 */
public enum RequestType {
    CREATE,
    READ,
    UPDATE,
    DELETE;

    public boolean matches(String toMatch) {
        return this.toString().equals(toMatch) || "*".equals(toMatch);
    }
}
