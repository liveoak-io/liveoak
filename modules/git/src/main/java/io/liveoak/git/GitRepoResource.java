/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.git;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.config.Configurable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
@Configurable
public class GitRepoResource extends GitDirectoryResource implements RootResource, GitResource {

    private Resource parent;
    private final String id;
    private final Vertx vertx;
    private final Repository repo;

    public GitRepoResource(String id, Repository repo, Vertx vertx) {
        super(null, repo.getWorkTree());
        this.id = id;
        this.repo = repo;
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
