/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.git;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import org.eclipse.jgit.api.Git;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class GitRepoResource extends GitDirectoryResource implements RootResource, GitResource {

    private final GitRepoAdminResource adminResource;
    private Resource parent;
    private final String id;
    private final Vertx vertx;

    public GitRepoResource(GitRepoAdminResource adminResource, String id, Vertx vertx) {
        super(null, adminResource.repository().getWorkTree());
        this.id = id;
        this.adminResource = adminResource;
        this.vertx = vertx;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public Vertx vertx() {
        return this.vertx;
    }

    @Override
    public Git git() {
        //return this.git;
        return null;
    }

    @Override
    public String id() {
        return this.id;
    }

    public String toString() {
        return "[GitRepoResource: repoPath=" + this.file.getAbsolutePath() + "]";
    }
}
