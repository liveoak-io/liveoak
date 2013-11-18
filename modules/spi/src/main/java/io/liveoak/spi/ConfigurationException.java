/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.spi;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ConfigurationException extends InitializationException {
    public ConfigurationException() {
        super();
    }

    public ConfigurationException( String message ) {
        super( message );
    }

    public ConfigurationException( String message, Throwable cause ) {
        super( message, cause );
    }

    public ConfigurationException( Throwable cause ) {
        super( cause );
    }
}
