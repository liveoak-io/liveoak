/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

/**
 * @author Bob McWhirter
 */
public class UpdateNotSupportedException extends ResourceException {

    public UpdateNotSupportedException( String path ) {
        super( path, "Update not supported for '" + path + "'" );
    }
}
