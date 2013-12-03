/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container;

/**
 * @author Bob McWhirter
 */
public class ResourceErrorResponse extends ResourceResponse {

    public enum ErrorType {
        NOT_AUTHORIZED,
        NOT_ACCEPTABLE,
        NO_SUCH_RESOURCE,
        RESOURCE_ALREADY_EXISTS,
        CREATE_NOT_SUPPORTED,
        READ_NOT_SUPPORTED,
        UPDATE_NOT_SUPPORTED,
        DELETE_NOT_SUPPORTED,
        INTERNAL_ERROR
    }

    public ResourceErrorResponse(ResourceRequest inReplyTo, ErrorType errorType) {
        super(inReplyTo, ResponseType.ERROR);
        this.errorType = errorType;
    }

    public ResourceErrorResponse(ResourceRequest inReplyTo, ErrorType errorType, String message) {
        super(inReplyTo, ResponseType.ERROR);
        this.errorType = errorType;
        this.message = message;
    }

    public ResourceErrorResponse(ResourceRequest inReplyTo, ErrorType errorType, Throwable cause) {
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
        return "[ResourceErrorResponse: type=" + this.errorType + "]";
    }

    private ErrorType errorType;
    private Throwable cause;
    private String message;
}
