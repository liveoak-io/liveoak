/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.exceptions;

import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class NotAcceptableException extends ResourceException {

    protected String message;

    public NotAcceptableException(String path) {
        super(path, "Request not acceptable");
    }

    public NotAcceptableException(String path, String errorCode) {
        this(path, errorCode, null);
    }

    public NotAcceptableException(String path, String message, Throwable cause) {
        super(path, "The request to resource at '" + path + " as not acceptable:" + message, cause);
        this.message = message;
    }

    public NotAcceptableException(String path, ResourceState state) {
        super(path, state);
    }

    public String message() {
        return this.message;
    }
}