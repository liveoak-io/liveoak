package io.liveoak.container.tenancy.service;

import java.io.File;
import java.util.function.Consumer;

import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Ken Finnigan
 */
public class ApplicationGitInstallCommitService implements Service<Void> {
    public ApplicationGitInstallCommitService(Consumer<File> gitCommit, File installDir) {
        this.gitCommit = gitCommit;
        this.installDir = installDir;
    }

    @Override
    public void start(StartContext context) throws StartException {
        try {
            this.gitCommit.accept(this.installDir);
        } catch (RuntimeException re) {
            log.error("Unable to commit to git on application install", re);
        } finally {
            // remove ourselves
            context.getController().setMode(ServiceController.Mode.REMOVE);
        }
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    private Consumer<File> gitCommit;
    private File installDir;
    private static final Logger log = Logger.getLogger(ApplicationGitInstallCommitService.class);
}
