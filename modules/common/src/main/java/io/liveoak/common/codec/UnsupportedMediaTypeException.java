/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.common.codec;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.MediaTypeMatcher;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public class UnsupportedMediaTypeException extends Exception {

    public UnsupportedMediaTypeException(MediaTypeMatcher matcher) {
        super("Unsupported media-types: " + matcher);
        this.matcher = matcher;
    }

    public MediaTypeMatcher matcher() {
        return this.matcher;
    }

    private MediaTypeMatcher matcher;
}
