package io.liveoak.filesystem.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;

/**
 * @author Bob McWhirter
 */
public class DirectoryService implements Service<File> {


    public DirectoryService(File dir) {
        this.dir = dir;
    }

    @Override
    public void start(StartContext context) throws StartException {
        File file = this.dir;
        if (configurationInjector.getValue().has("dir")) {
            file = new File(configurationInjector.getValue().get("dir").asText());
        }

        try {
            ensure(file);
            this.actualDir = file;
        } catch (Exception e) {
            throw new StartException(e);
        }
    }

    protected void ensure(File dir) throws Exception {
        dir.mkdirs();
        if (!dir.exists()) {
            throw new NoSuchFileException(this.dir.getAbsolutePath());
        }
        if (!dir.isDirectory()) {
            throw new NotDirectoryException(this.dir.getAbsolutePath());
        }
        if (!dir.canRead()) {
            throw new AccessDeniedException(this.dir.getAbsolutePath());
        }
    }

    @Override
    public void stop(StopContext context) {
        this.dir = null;
    }

    @Override
    public File getValue() throws IllegalStateException, IllegalArgumentException {
        return this.actualDir;
    }

    public Injector<ObjectNode> configurationInjector() {
        return this.configurationInjector;
    }

    private InjectedValue<ObjectNode> configurationInjector = new InjectedValue<>();
    private File dir;
    private File actualDir;
}
