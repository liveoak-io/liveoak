package io.liveoak.container.tenancy.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class ApplicationsDirectoryService implements Service<File> {

    public ApplicationsDirectoryService(File appsDir) {
        this.appsDir = appsDir;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.appsDir.mkdirs();
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public File getValue() throws IllegalStateException, IllegalArgumentException {
        return this.appsDir;
    }

    private File appsDir;

}
