package io.liveoak.spi;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ResourceAlreadyExistsException extends ResourceException {

    public ResourceAlreadyExistsException(String path) {
        super(path, "Resource with id '" + path + "' already exists." );
    }
}

