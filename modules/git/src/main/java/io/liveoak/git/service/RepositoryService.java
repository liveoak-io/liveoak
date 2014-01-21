package io.liveoak.git.service;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.io.File;
import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class RepositoryService implements Service<Repository> {

    public RepositoryService(File dir, String id) {
        this.dir = dir;
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        try {
            this.repository = new RepositoryBuilder()
                    .setWorkTree(new File( this.dir, this.id ) )
                    .build();
        } catch (IOException e) {
            throw new StartException( e );
        }
    }

    @Override
    public void stop(StopContext context) {
        this.repository.close();
    }

    @Override
    public Repository getValue() throws IllegalStateException, IllegalArgumentException {
        return this.repository;
    }

    private final File dir;
    private final String id;

    private Repository repository;
}
