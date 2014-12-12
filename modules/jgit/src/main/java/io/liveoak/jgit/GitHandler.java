package io.liveoak.jgit;

import java.util.concurrent.Callable;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author Ken Finnigan
 */
public final class GitHandler {

    private final static Object gitLock = new Object();

    public static RevCommit commit(Callable<RevCommit> gitCommand) throws GitAPIException {
        RevCommit commit = null;

        synchronized (gitLock) {
            try {
                commit = gitCommand.call();
            } catch (GitAPIException e) {
                throw e;
            } catch (Exception e) {
                throw new GitAPIException("Error invoking Callable.", e) {};
            }
        }

        return commit;
    }
}
