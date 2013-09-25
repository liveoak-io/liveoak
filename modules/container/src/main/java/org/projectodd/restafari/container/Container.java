package org.projectodd.restafari.container;

import java.util.HashMap;
import java.util.Map;

import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.container.codec.ToStringCodec;
import org.projectodd.restafari.spi.ResourceController;


public class Container {

    public Container() {
        this.codecManager.registerResourceCodec( "text/plain", new ToStringCodec() );
    }
    
    public void registerResourceController(String type, ResourceController controller) {
        this.controllers.put( type, new Holder( controller ) );
    }
    
    public Holder getResourceController(String type) {
        return this.controllers.get( type );
    }
    
    public void registerResourceCodec(String mimeType, ResourceCodec codec) {
        this.codecManager.registerResourceCodec(mimeType, codec);
    }
    
    public ResourceCodec getResourceCodec(String mimeType) {
        return this.codecManager.getResourceCodec(mimeType);
    }
    
    public ResourceCodecManager getCodecManager() {
        return this.codecManager;
    }
    
    private Map<String,Holder> controllers = new HashMap<>();
    private ResourceCodecManager codecManager = new ResourceCodecManager();

}
