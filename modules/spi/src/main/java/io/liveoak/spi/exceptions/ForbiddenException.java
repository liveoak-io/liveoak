/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.exceptions;

import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public class ForbiddenException extends ResourceException {

    public ForbiddenException(String path) {
        super(path, "Not allowed access to '" + path + "'");
    }

    public ForbiddenException(String path, String message) {
        super(path, message);
    }

    public ForbiddenException(String path, ResourceState state) {
        super(path, state);
    }
}
