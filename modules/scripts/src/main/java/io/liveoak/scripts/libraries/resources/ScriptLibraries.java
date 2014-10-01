package io.liveoak.scripts.libraries.resources;

import io.liveoak.scripts.libraries.manager.Library;
import io.liveoak.scripts.libraries.manager.LibraryManager;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ScriptLibraries implements Resource {

    private Resource parent;
    private static final String ID = "libraries";

    private LibraryManager libraryManager;

    public ScriptLibraries(LibraryManager libraryManager) {
        this.libraryManager = libraryManager;
    }

    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("name", "Script Libraries");
        sink.accept("description", "Libraries which can be exposed to individual script resources.");
        sink.accept("count", libraryManager.getLibraries().size());
        sink.close();
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        try {
            for (Library library : libraryManager.getLibraries().values()) {
                LibraryResource libraryResource = new LibraryResource(this, library);
                sink.accept(libraryResource);
            }
        } catch (Throwable e) {
            sink.error(e);
        } finally {
            sink.close();
        }
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        Library library = libraryManager.getLibrary(id);
        if (library != null) {
            LibraryResource libraryResource = new LibraryResource(this, library);
            responder.resourceRead(libraryResource);
        } else {
            responder.noSuchResource(id);
        }
    }

}
