package org.projectodd.restafari.container.codec;

import org.projectodd.restafari.spi.MediaType;
import org.projectodd.restafari.spi.resource.async.BinaryResource;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public class IncompatibleMediaTypeException extends Exception {

    public IncompatibleMediaTypeException(List<MediaType> requestedTypes, BinaryResource resource) {
        super( "Resource type '" + resource.mediaType() + "' is not compatible with requested types: " + requestedTypes );
        this.requestedTypes = requestedTypes;
        this.resource = resource;
    }

    public List<MediaType> requestedMediaTypes() {
        return this.requestedTypes;
    }

    public BinaryResource resource() {
        return this.resource;
    }

    private List<MediaType> requestedTypes;
    private BinaryResource resource;
}
