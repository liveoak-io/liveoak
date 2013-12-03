/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.spi;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ResourceProcessingException extends Exception {

    public ResourceProcessingException(String message) {
        super(message);
    }

    public ResourceProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}
