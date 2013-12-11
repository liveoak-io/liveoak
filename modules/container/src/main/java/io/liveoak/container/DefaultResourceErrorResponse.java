/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourceRequest;

/**
 * @author Bob McWhirter
 */
public class DefaultResourceErrorResponse extends DefaultResourceResponse implements ResourceErrorResponse {



    public DefaultResourceErrorResponse(ResourceRequest inReplyTo, ErrorType errorType) {
        super(inReplyTo, ResponseType.ERROR);
        this.errorType = errorType;
    }

    public DefaultResourceErrorResponse(ResourceRequest inReplyTo, ErrorType errorType, String message) {
        super(inReplyTo, ResponseType.ERROR);
        this.errorType = errorType;
        this.message = message;
    }

    public DefaultResourceErrorResponse(ResourceRequest inReplyTo, ErrorType errorType, String message, Throwable cause) {
        super(inReplyTo, ResponseType.ERROR);
        this.errorType = errorType;
        this.cause = cause;
        this.message = message;
    }

    public DefaultResourceErrorResponse(ResourceRequest inReplyTo, ErrorType errorType, Throwable cause) {
        super(inReplyTo, ResponseType.ERROR);
        this.errorType = errorType;
        this.cause = cause;
    }

    public Throwable cause() {
        return this.cause;
    }

    public String message() {
        if ( this.message != null ) {
            return this.message;
        }

        if ( this.cause != null ) {
            return this.cause.getMessage();
        }
        return null;
    }


    public ErrorType errorType() {
        return this.errorType;
    }

    public String toString() {
        return "[DefaultResourceErrorResponse: type=" + this.errorType + "]";
    }

    private ErrorType errorType;
    private Throwable cause;
    private String message;
}
