package org.projectodd.restafari.container;

import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.ResourceController;
import org.projectodd.restafari.spi.Responder;

public class Holder {
    
    public Holder(ResourceController controller) {
        this.controller = controller;
    }
    
    public ResourceController getResourceController() {
        return this.controller;
    }

    private ResourceController controller;

}
