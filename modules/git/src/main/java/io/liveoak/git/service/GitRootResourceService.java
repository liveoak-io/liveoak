package io.liveoak.git.service;

import java.io.File;

import io.liveoak.git.GitRootResource;
import io.liveoak.spi.resource.RootResource;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Ken Finnigan
 */
public class GitRootResourceService implements Service<RootResource> {
    public GitRootResourceService(String id, File applicationDir) {
        this.id = id;
        this.applicationDir = applicationDir;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.gitRoot = new GitRootResource(id, applicationDir);
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public RootResource getValue() throws IllegalStateException, IllegalArgumentException {
        return gitRoot;
    }

    private RootResource gitRoot;
    private String id;
    private File applicationDir;
}
