/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.resource.async;

import io.liveoak.spi.ResourceErrorResponse;

/**
 * @author Bob McWhirter
 */
public class DelegatingResponder implements Responder {

    public DelegatingResponder(Responder delegate) {
        this.delegate = delegate;
    }

    @Override
    public void resourceRead(Resource resource) {
        delegate.resourceRead(resource);
    }

    @Override
    public void resourceCreated(Resource resource) {
        delegate.resourceCreated(resource);
    }

    @Override
    public void resourceDeleted(Resource resource) {
        delegate.resourceDeleted(resource);
    }

    @Override
    public void resourceUpdated(Resource resource) {
        delegate.resourceUpdated(resource);
    }

    @Override
    public void createNotSupported(Resource resource) {
        delegate.createNotSupported(resource);
    }

    @Override
    public void readNotSupported(Resource resource) {
        delegate.readNotSupported(resource);
    }

    @Override
    public void updateNotSupported(Resource resource) {
        delegate.updateNotSupported(resource);
    }

    @Override
    public void deleteNotSupported(Resource resource) {
        delegate.deleteNotSupported(resource);
    }

    @Override
    public void noSuchResource(String id) {
        delegate.noSuchResource(id);
    }

    @Override
    public void resourceAlreadyExists(String id) {
        delegate.resourceAlreadyExists(id);
    }

    @Override
    public void internalError(String message) {
        delegate.internalError(message);
    }

    @Override
    public void internalError(String message, Throwable cause) {
        delegate.internalError(message, cause);
    }

    @Override
    public void internalError(Throwable cause) {
        delegate.internalError(cause);
    }

    @Override
    public void invalidRequest(String message) {
        delegate.invalidRequest(message);
    }

    @Override
    public void invalidRequest(Throwable cause) {
        delegate.invalidRequest(cause);
    }

    @Override
    public void invalidRequest(String message, Throwable cause) {
        delegate.invalidRequest(message, cause);
    }

    @Override
    public void error(ResourceErrorResponse.ErrorType errorType) {
        delegate.error(errorType);
    }

    @Override
    public void error(ResourceErrorResponse.ErrorType errorType, String message) {
        delegate.error(errorType, message);
    }

    @Override
    public void error(ResourceErrorResponse.ErrorType errorType, String message, Throwable cause) {
        delegate.error(errorType, message, cause);
    }

    private Responder delegate;
}
