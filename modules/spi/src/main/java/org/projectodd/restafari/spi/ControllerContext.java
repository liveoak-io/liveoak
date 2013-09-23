package org.projectodd.restafari.spi;

import org.vertx.java.core.Vertx;

public interface ControllerContext {
    
    Vertx getVertx();
    Container getContainer();
    Config getConfig();
    

}
