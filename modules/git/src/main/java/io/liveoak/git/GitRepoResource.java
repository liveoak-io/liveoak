/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.git;

import java.io.File;
import java.util.HashMap;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.config.ConfigMappingExporter;
import io.liveoak.spi.resource.config.ConfigProperty;
import io.liveoak.spi.resource.config.Configurable;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
@Configurable
public class GitRepoResource extends GitDirectoryResource implements RootResource, GitResource {

    private String id;
    private Vertx vertx;

    private Git git;

    public GitRepoResource(String id) {
        super(null, null);
        this.id = id;
    }

    private void updateConfig(@ConfigProperty("repoPath") String repoPathStr, @ConfigProperty("createIfMissing") Boolean createRepo) throws Exception {
        if (repoPathStr == null) {
            throw new InitializationException("no git repo path specified");
        }

        File file = new File(repoPathStr);

        if (!file.canRead()) {
            throw new InitializationException("unable to readMember git repo at: " + file.getAbsolutePath());
        }

        if (createRepo == null) {
            createRepo = Boolean.FALSE;
        }

        Repository repo = new RepositoryBuilder()
                .setWorkTree(file)
                .build();
        Git git = new Git(repo);

        if (!repo.getDirectory().exists()) {
            if (createRepo) {
                // No git repo, need to create
                git.getRepository().create();
            } else {
                throw new InitializationException("unable to setup repository at: " + file.getAbsolutePath()
                        + "; no git repository found");
            }
        }

        this.git(git);
        this.file(file);
    }

    @ConfigMappingExporter
    public void exportConfig(HashMap<String, Object> config) throws Exception {
        config.put("repoPath", this.git().getRepository().getDirectory().getAbsolutePath());
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
}
