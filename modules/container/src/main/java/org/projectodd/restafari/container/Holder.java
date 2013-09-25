package org.projectodd.restafari.container;

import org.projectodd.restafari.spi.ResourceController;

public class Holder {
    
    public Holder(ResourceController controller) {
        this.controller = controller;
    }
    
    public ResourceController getResourceController() {
        return this.controller;
    }

    private ResourceController controller;

}
