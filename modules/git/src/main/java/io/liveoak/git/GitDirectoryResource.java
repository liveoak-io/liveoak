/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.git;

import io.liveoak.filesystem.DirectoryResource;
import io.liveoak.filesystem.FileResource;
import org.eclipse.jgit.api.Git;

import java.io.File;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class GitDirectoryResource extends DirectoryResource implements GitResource {

    private GitDirectoryResource parent;

    public GitDirectoryResource(GitDirectoryResource parent, File file) {
        super(parent, file);
        this.parent = parent;
    }

    @Override
    public Git git() {
        return this.parent.git();
    }

    @Override
    protected DirectoryResource createDirectoryResource(File path) {
        return new GitDirectoryResource(this, path);
    }

    @Override
    protected FileResource createFileResource(File file) {
        return new GitFileResource(this, file);
    }
}
