/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.git;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.Configurable;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import org.eclipse.jgit.api.Git;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
@Configurable
public class GitRepoResource extends GitDirectoryResource implements RootResource, GitResource {

    private String id;
    private Vertx vertx;
    private Git git;
    private GitRepoConfigResource configResource;

    public GitRepoResource(String id) {
        super(null, null);
        this.id = id;
        this.configResource = new GitRepoConfigResource(this);
    }

    @Override
    public Vertx vertx() {
        return this.vertx;
    }

    @Override
    public Git git() {
        return this.git;
    }

    public void git(Git git) {
        this.git = git;
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
        this.vertx = context.vertx();
    }

    @Override
    public void destroy() {
        if (this.git != null) {
            this.git.getRepository().close();
        }
    }

    @Override
    public String id() {
        return this.id;
    }

    public String toString() {
        return "[GitRepoResource: repoPath=" + this.file.getAbsolutePath() + "]";
    }

    @Override
    public Resource configuration() {
        return this.configResource;
    }
}
