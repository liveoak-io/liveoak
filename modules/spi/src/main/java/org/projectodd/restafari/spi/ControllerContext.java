package org.projectodd.restafari.spi;

import org.vertx.java.core.Vertx;

/** Resource-controller context.
 * 
 * @author Bob McWhirter
 */
public interface ControllerContext {
    
    /** Retrieve the Vertx.
     * 
     * @return The Vertx.
     */
    Vertx getVertx();
    
    /** Retrieve the container.
     * 
     * @return The container.
     */
    Container getContainer();
    
    /** Retrieve the controller config.
     * 
     * @return The controller config.
     */
    Config getConfig();
    

}
