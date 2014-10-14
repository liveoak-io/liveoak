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
    private static final String TESTAPP_GIT_ADMIN_ROOT = ADMIN_ROOT + "testApp" + GIT_ADMIN_RESOURCE_PATH;
    private static final String TESTAPP_GIT_PUBLIC_ROOT = "/testApp/git";

    @Override
    public void loadExtensions() throws Exception {
        loadExtension("git", new GitExtension());
    }

    @Test
    public void rootResource() throws Exception {
        // Test #1 -  Git Repo already exists
        File alreadyGitDir = new File(this.application.directory(), ".git");

        // Verify git dir exists
        assertThat(alreadyGitDir.exists()).isTrue();
        assertThat(new File(alreadyGitDir, ".git").exists()).isFalse();

        // Verify git resource is installed
        assertThat(execGet(TESTAPP_GIT_ADMIN_ROOT)).hasStatus(200);
        assertThat(execGet(TESTAPP_GIT_PUBLIC_ROOT)).hasStatus(404).hasNoSuchResource();

        // Verify git dir still exists, and we didn't create it too low in folder hierarchy
        assertThat(alreadyGitDir.exists()).isTrue();
        assertThat(new File(alreadyGitDir, ".git").exists()).isFalse();

    }

}
