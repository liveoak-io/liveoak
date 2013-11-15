/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.git;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.ResourceContext;
import io.liveoak.spi.resource.RootResource;
import org.vertx.java.core.Vertx;

import java.io.File;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class GitResource extends GitDirectoryResource implements RootResource {

    private String id;
    private Vertx vertx;

    public GitResource() {
        super( null, null );
    }

    public GitResource(String id) {
        super( null, null );
        this.id = id;
    }

    @Override
    public Vertx vertx() {
        return this.vertx;
    }

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
        if (this.id == null) {
            this.id = context.config().get("id", null);
            if (this.id == null) {
                throw new InitializationException("no id specified");
            }
        }

        String repoPathStr = context.config().get("repoPath", null);
        if (repoPathStr == null) {
            throw new InitializationException("no git repo path specified");
        }

        this.file = new File(repoPathStr);

        if (!this.file.canRead()) {
            throw new InitializationException("unable to readMember git repo at: " + this.file.getAbsolutePath());
        }

        this.vertx = context.vertx();
    }

    @Override
    public void destroy() {
        // Do Nothing
    }

    @Override
    public String id() {
        return this.id;
    }

    public String toString() {
        return "[GitResource: repoPath=" + this.file.getAbsolutePath() + "]";
    }
}
