/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.git;

import io.liveoak.filesystem.FileResource;

import java.io.File;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class GitFileResource extends FileResource {

    public GitFileResource(GitDirectoryResource parent, File file) {
        super(parent, file);
    }
}
