package io.liveoak.spi;

import io.liveoak.spi.state.ResourceState;

public interface BinaryResourceState extends ResourceState {
    
    String getMimeType();

}
