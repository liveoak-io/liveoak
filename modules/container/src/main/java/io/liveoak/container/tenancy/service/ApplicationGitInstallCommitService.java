package io.liveoak.container.tenancy.service;

import java.io.File;
import java.util.function.Consumer;

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
        this.gitCommit.accept(this.installDir);

        // remove ourselves
        context.getController().setMode(ServiceController.Mode.REMOVE);
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
}
