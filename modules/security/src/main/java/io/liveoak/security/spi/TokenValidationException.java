/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.security.spi;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TokenValidationException extends Exception {

    public TokenValidationException( String message ) {
        super( message );
    }

    public TokenValidationException( String message, Exception cause ) {
        super( message, cause );
    }
}
