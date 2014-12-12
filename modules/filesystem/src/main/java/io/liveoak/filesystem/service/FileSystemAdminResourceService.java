package io.liveoak.filesystem.service;

import java.io.File;

import io.liveoak.filesystem.FileSystemAdminResource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.vertx.java.core.Vertx;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class FileSystemAdminResourceService implements Service<FileSystemAdminResource> {

    FileSystemAdminResource adminResource;
    String id;
    File initialDirectory;

    public FileSystemAdminResourceService(String id, File initialDirectory) {
        this.id = id;
        this.initialDirectory = initialDirectory;
    }

    @Override
    public void start(StartContext context) throws StartException {
        adminResource = new FileSystemAdminResource(id, initialDirectory, vertxInjector.getValue());
    }

    @Override
    public void stop(StopContext context) {
        //Do nothing for now
    }

    @Override
    public FileSystemAdminResource getValue() throws IllegalStateException, IllegalArgumentException {
        return adminResource;
    }

    public Injector<Vertx> vertxInjector() {
        return this.vertxInjector;
    }
    protected InjectedValue<Vertx> vertxInjector = new InjectedValue<>();
}
