/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.git;

import java.io.File;

import io.liveoak.git.extension.GitExtension;
import io.liveoak.testtools.AbstractHTTPResourceTestCase;
import org.junit.Test;

import static io.liveoak.testtools.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class HTTPGitResourceTest extends AbstractHTTPResourceTestCase {
    private static final String ADMIN_ROOT = "/admin/applications/";
    private static final String GIT_ADMIN_RESOURCE_PATH = "/resources/git";
    private static final String GIT_ADMIN_ROOT = ADMIN_ROOT + "newApp" + GIT_ADMIN_RESOURCE_PATH;
    private static final String GIT_PUBLIC_ROOT = "/newApp/git";

    @Override
    public void loadExtensions() throws Exception {
        loadExtension("git", new GitExtension());
    }

    @Test
    public void rootResource() throws Exception {
        // Test #1 -  Git Repo present after install

        File appDir = new File(this.application.directory().getParentFile(), "newApp");
        File appGitDir = new File(appDir, ".git");

        // Verify app and git dir do not exist
        assertThat(appDir.exists()).isFalse();
        assertThat(appGitDir.exists()).isFalse();

        // Create new app
        assertThat(execPost(ADMIN_ROOT, "{ \"id\": \"newApp\" }")).hasStatus(201);
        this.system.awaitStability();

        // Verify app and git dirs exist
        assertThat(appDir.exists()).isTrue();
        assertThat(appGitDir.exists()).isTrue();
        assertThat(new File(appGitDir, ".git").exists()).isFalse();

        // Verify REST endpoints
        assertThat(execGet(GIT_ADMIN_ROOT)).hasStatus(200);
        assertThat(execGet(GIT_PUBLIC_ROOT)).hasStatus(404).hasNoSuchResource();

    }

}
