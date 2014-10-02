/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi.exceptions;

/**
 * @author Bob McWhirter
 */
public class DeleteNotSupportedException extends ResourceException {

    public DeleteNotSupportedException(String path) {
        super(path, "Delete not supported for '" + path + "'");
    }
}
