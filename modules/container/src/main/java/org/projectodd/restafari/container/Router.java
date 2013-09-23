package org.projectodd.restafari.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.projectodd.restafari.spi.ResourceController;

public class Router {
    
    public Router() {
        
    }
    
    public synchronized void addRoute(String path, ResourceController controller) {
        this.routes.add( new Route( path, controller ) );
        Collections.sort( this.routes );
    }
    
    public Route findMatchingRoute(String path) {
        for ( Route each : this.routes ) {
            if ( path.startsWith( each.getPath() ) ) {
                return each;
            }
        }
        
        return null;
    }
    
    private List<Route> routes = new ArrayList<>();

}
