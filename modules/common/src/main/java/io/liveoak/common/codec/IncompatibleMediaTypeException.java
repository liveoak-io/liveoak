/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec;

import io.liveoak.spi.MediaTypeMatcher;
import io.liveoak.spi.resource.async.BinaryResource;

/**
 * @author Bob McWhirter
 */
public class IncompatibleMediaTypeException extends Exception {

    public IncompatibleMediaTypeException(MediaTypeMatcher matcher, BinaryResource resource) {
        super("Resource type '" + resource.mediaType() + "' is not compatible with requested types: " + matcher);
        this.matcher = matcher;
        this.resource = resource;
    }

    public MediaTypeMatcher matcher() {
        return this.matcher;
    }

    public BinaryResource resource() {
        return this.resource;
    }

    private MediaTypeMatcher matcher;
    private BinaryResource resource;
}
