/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec;

import io.liveoak.spi.MediaType;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public class UnsupportedMediaTypeException extends Exception {

    public UnsupportedMediaTypeException(List<MediaType> mediaTypes) {
        super( "Unsupported media-types: " + mediaTypes );
        this.mediaTypes = mediaTypes;
    }

    public List<MediaType> mediaTypes() {
        return this.mediaTypes;
    }

    private List<MediaType> mediaTypes;
}
