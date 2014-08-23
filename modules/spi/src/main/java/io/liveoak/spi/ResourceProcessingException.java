/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.spi;

import static io.liveoak.spi.ResourceErrorResponse.*;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ResourceProcessingException extends Exception {

    private final ErrorType errorType;

    public ResourceProcessingException(String message) {
        this(ErrorType.INTERNAL_ERROR, message);
    }

    public ResourceProcessingException(String message, Throwable cause) {
        this(ErrorType.INTERNAL_ERROR, message, cause);
    }

    public ResourceProcessingException(ErrorType errorType) {
        this.errorType = errorType;
    }

    public ResourceProcessingException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ResourceProcessingException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType errorType() {
        return errorType;
    }
}
