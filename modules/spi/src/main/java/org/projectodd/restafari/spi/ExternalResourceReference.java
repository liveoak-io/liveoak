package org.projectodd.restafari.spi;

import java.net.URI;

public class ExternalResourceReference implements ResourceReference {
    
    public ExternalResourceReference(URI uri) {
        this.uri = uri;
    }
    
    public URI getURI() {
        return this.uri;
    }

    private URI uri;

}
