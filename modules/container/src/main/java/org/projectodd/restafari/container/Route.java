package org.projectodd.restafari.container;

import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.ResourceController;
import org.projectodd.restafari.spi.Responder;

public class Route implements Comparable<Route> {

    public Route(String path, ResourceController controller) {
        if ( path.endsWith( "/" ) ) {
            path = path.substring( 0, path.length() - 1 );
        }
        int lastSlash = path.lastIndexOf( '/' );
        this.collectionName = path.substring( lastSlash + 1 );
        this.path = path;
        this.controller = controller;
    }
    
    public String getPath() {
        return this.path;
    }
    
    public String getCollectionName() {
        return this.collectionName;
    }
    
    public ResourceController getResourceController() {
        return this.controller;
    }
    
    public void getResource(RequestContext context, String resourceId, Responder responder) {
        this.controller.getResource(context, this.collectionName, resourceId, responder );
    }
    
    public void getResources(RequestContext context, Responder responder) {
        this.controller.getResources(context, this.collectionName, null, responder );
    }
    
    public int compareTo(Route o) {
        if ( this.path.length() > o.path.length() ) {
            return -1;
        } else if ( this.path.length() < o.path.length() ) {
            return 1;
        }
        return 0;
    }

    private String path;
    private String collectionName;
    private ResourceController controller;
}
    