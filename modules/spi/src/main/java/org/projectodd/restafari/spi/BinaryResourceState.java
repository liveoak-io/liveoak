package org.projectodd.restafari.spi;

import org.projectodd.restafari.spi.state.ResourceState;

public interface BinaryResourceState extends ResourceState {
    
    String getMimeType();

}
