package org.projectodd.restafari.container;

import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.container.codec.ToStringCodec;
import org.projectodd.restafari.spi.ResourceController;


public class Container {

    public Container() {
        this.codecManager.registerResourceCodec( "text/plain", new ToStringCodec() );
    }
    
    public void registerResourceController(String path, ResourceController controller) {
        router.addRoute(path, controller);
    }
    
    public Route findMatchingRoute(String path) {
        return this.router.findMatchingRoute(path);
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
    
    private Router router = new Router();
    private ResourceCodecManager codecManager = new ResourceCodecManager();

}
