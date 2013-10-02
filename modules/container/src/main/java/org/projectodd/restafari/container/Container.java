package org.projectodd.restafari.container;

import java.util.HashMap;
import java.util.Map;

import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.container.codec.json.JSONCodec;
import org.projectodd.restafari.spi.Config;
import org.projectodd.restafari.spi.InitializationException;
import org.projectodd.restafari.spi.ResourceController;


public class Container {

    public Container() {
        this.codecManager.registerResourceCodec( "application/json", new JSONCodec() );
    }

    public void registerResourceController(String type, ResourceController controller, Config config) throws InitializationException {
        //TODO: Can probably delegate the initialization to the holder when the first get is called (delaying initialization)
        // we can only initialize controllers as they are needed
        controller.initialize(new SimpleControllerContext(null, null, config));

        this.controllers.put( type, new Holder( controller ) );
    }

    public Holder getResourceController(String type) {
        return this.controllers.get( type );
    }

    public ResourceCodecManager getCodecManager() {
        return this.codecManager;
    }

    private Map<String,Holder> controllers = new HashMap<>();
    private ResourceCodecManager codecManager = new ResourceCodecManager();
}
