/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.git;

import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;

import io.liveoak.git.extension.GitExtension;
import io.liveoak.testtools.AbstractHTTPResourceTestCaseWithTestApp;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.liveoak.testtools.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class HTTPGitResourceTest extends AbstractHTTPResourceTestCaseWithTestApp {
    private static final String ADMIN_ROOT = "/admin/applications/";
    private static final String GIT_ADMIN_RESOURCE_PATH = "/resources/git";
    private static final String GIT_ADMIN_ROOT = ADMIN_ROOT + "newApp" + GIT_ADMIN_RESOURCE_PATH;
    private static final String GIT_PUBLIC_ROOT = "/newApp/git";

    @BeforeClass
    public static void loadExtensions() throws Exception {
        loadExtension("git", new GitExtension());
    }

    @Test
    public void rootResource() throws Exception {
        // Test #1 -  Git Repo present after install
        File appDir = new File(testApplication.directory().getParentFile(), "newApp");
        File appGitDir = new File(appDir, ".git");

        // Verify app and git dir do not exist
        assertThat(appDir.exists()).isFalse();
        assertThat(appGitDir.exists()).isFalse();

        // Create new app
        assertThat(execPost(ADMIN_ROOT, "{ \"id\": \"newApp\" }")).hasStatus(201);
        awaitStability();

        // Verify app and git dirs exist
        assertThat(appDir.exists()).isTrue();
        assertThat(appGitDir.exists()).isTrue();
        assertThat(new File(appGitDir, ".git").exists()).isFalse();

        // Verify REST endpoints
        assertThat(execGet(GIT_ADMIN_ROOT)).hasStatus(200);
        assertThat(execGet(GIT_PUBLIC_ROOT)).hasStatus(404).hasNoSuchResource();


        // Test #2 - Post a commit, with auto add files to index
        // Create a file in the application directory
        File testFile = new File(appDir, "test.txt");
        assertThat(testFile.createNewFile()).isTrue();
        Files.write(testFile.toPath(), "content".getBytes());

        Git gitRepo = Git.open(appDir);

        // Execute a commit
        assertThat(execPost("/admin/applications/newApp/resources/git/commits", "{ \"msg\": \"my commit message\" }")).hasStatus(201);

        assertThat(gitRepo.status().call().hasUncommittedChanges()).isFalse();
        Iterator<RevCommit> iterator = gitRepo.log().all().call().iterator();
        int commitSize = 0;
        RevCommit latestCommit = null;
        while (iterator.hasNext()) {
            RevCommit commit = iterator.next();
            if (commitSize == 0) {
                latestCommit = commit;
            }
            commitSize++;
        }

        assertThat(commitSize).isEqualTo(2);
        assertThat(latestCommit.getFullMessage()).isEqualTo("my commit message");

        TreeWalk treeWalk = new TreeWalk(gitRepo.getRepository());
        treeWalk.addTree(latestCommit.getTree());
        treeWalk.setFilter(PathFilter.create("test.txt"));
        assertThat(treeWalk.next()).isTrue();
        String fileContent = new String(treeWalk.getObjectReader().open(treeWalk.getObjectId(0)).getBytes());
        treeWalk.release();
        assertThat(fileContent).isEqualTo("content");


        // Test #3 - Post a commit, with auto add files to index off
        File anotherFile = new File(appDir, "another.txt");
        assertThat(anotherFile.createNewFile()).isTrue();
        Files.write(anotherFile.toPath(), "another content".getBytes());
        Files.write(testFile.toPath(), "updated content".getBytes());

        // Execute a commit
        assertThat(execPost("/admin/applications/newApp/resources/git/commits", "{ \"msg\": \"another commit\", \"include-untracked\": \"false\" }")).hasStatus(201);

        assertThat(gitRepo.status().call().isClean()).isFalse();
        iterator = gitRepo.log().all().call().iterator();
        commitSize = 0;
        while (iterator.hasNext()) {
            RevCommit commit = iterator.next();
            if (commitSize == 0) {
                latestCommit = commit;
            }
            commitSize++;
        }

        assertThat(commitSize).isEqualTo(3);
        assertThat(latestCommit.getFullMessage()).isEqualTo("another commit");

        treeWalk = new TreeWalk(gitRepo.getRepository());
        treeWalk.addTree(latestCommit.getTree());
        treeWalk.setFilter(PathFilter.create("another.txt"));
        assertThat(treeWalk.next()).isFalse();
        treeWalk.release();

        treeWalk = new TreeWalk(gitRepo.getRepository());
        treeWalk.addTree(latestCommit.getTree());
        treeWalk.setFilter(PathFilter.create("test.txt"));
        assertThat(treeWalk.next()).isTrue();

        fileContent = new String(treeWalk.getObjectReader().open(treeWalk.getObjectId(0)).getBytes());
        treeWalk.release();
        assertThat(fileContent).isEqualTo("updated content");


        // Test #4 - Verify PUT on commit is not supported
        assertThat(execPut("/admin/applications/newApp/resources/git/commits/" + latestCommit.getName(), "{ \"bad\": \"request\" }")).hasStatus(500).isInternalError();
    }

}
