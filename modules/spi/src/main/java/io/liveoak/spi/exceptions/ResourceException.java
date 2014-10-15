/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.exceptions;

import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ResourceException extends Exception {

    public ResourceException(String path) {
        this.path = path;
    }

    public ResourceException(String path, String message) {
        super(message);
        this.path = path;
    }

    public ResourceException(String path, String message, Throwable cause) {
        super(message, cause);
        this.path = path;
    }

    public ResourceException(String path, Throwable cause) {
        super(cause);
        this.path = path;
    }

    public ResourceException(String path, ResourceState state) {
        this.path = path;
        this.state = state;
    }

    public ResourceException(String path, String message, ResourceState state) {
        super(message);
        this.path = path;
        this.state = state;
    }

    public String path() {
        return this.path;
    }

    public void state(ResourceState state) {
        this.state = state;
    }

    public ResourceState state() {
        return state;
    }

    private String path;
    private ResourceState state;
}
