/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.resource.async.BinaryResource;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public class IncompatibleMediaTypeException extends Exception {

    public IncompatibleMediaTypeException(List<MediaType> requestedTypes, BinaryResource resource) {
        super( "Resource type '" + resource.mediaType() + "' is not compatible with requested types: " + requestedTypes );
        this.requestedTypes = requestedTypes;
        this.resource = resource;
    }

    public List<MediaType> requestedMediaTypes() {
        return this.requestedTypes;
    }

    public BinaryResource resource() {
        return this.resource;
    }

    private List<MediaType> requestedTypes;
    private BinaryResource resource;
}
