/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

/**
 * @author Bob McWhirter
 */
public class CreateNotSupportedException extends ResourceException {

    public CreateNotSupportedException(String path) {
        super(path, "Create not supported for '" + path + "'");
    }

}
