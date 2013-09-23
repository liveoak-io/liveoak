package org.projectodd.restafari.container.codec;

import java.util.HashMap;
import java.util.Map;

public class ResourceCodecManager {
    
    public ResourceCodec getResourceCodec(String mimeType) {
        return this.codecs.get( mimeType );
    }
    
    public void registerResourceCodec(String mimeType, ResourceCodec codec) {
        this.codecs.put( mimeType, codec);
    }
    
    
    private Map<String,ResourceCodec> codecs = new HashMap<>();

    
}
