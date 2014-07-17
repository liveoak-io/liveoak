package io.liveoak.mongo.gridfs.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class TmpDirService implements Service<File> {

    @Override
    public void start(StartContext context) throws StartException {
        try {
            Path result = Files.createTempDirectory("liveoak-gridfs");
            this.dir = result.toFile();
            this.dir.deleteOnExit();
        } catch (IOException e) {
            throw new StartException(e);
        }
    }

    @Override
    public void stop(StopContext context) {
        delete(this.dir);
    }

    protected void delete(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (File child : children) {
                delete(child);
            }
        }

        file.delete();
    }

    @Override
    public File getValue() throws IllegalStateException, IllegalArgumentException {
        return this.dir;
    }

    private File dir;
}
