/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

/**
 * @author Bob McWhirter
 */
public class ResourceException extends Exception {

    protected ResourceException(String path) {
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

    public String path() {
        return this.path;
    }

    private String path;
}
