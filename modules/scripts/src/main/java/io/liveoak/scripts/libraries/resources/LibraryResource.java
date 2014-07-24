package io.liveoak.scripts.libraries.resources;

import io.liveoak.scripts.libraries.manager.Library;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LibraryResource implements Resource {

    Resource parent;
    String id;

    String name;
    String description;

    public LibraryResource(String id, Resource parent, String name, String description) {
        this.parent = parent;
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public LibraryResource(Resource parent, Library library) {
        this(library.name(), parent, library.name(), library.description());
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("name", name);
        sink.accept("description", description);
        sink.close();
    }

    //TODO: add in more stuff here? Instructions on how to use the library? methods/functions/etc...
    // add a link to the documentation?
    // JSDoc?
}
