package io.liveoak.spi;

import org.vertx.java.core.Vertx;

/** Resource initialization context.
 * 
 * @author Bob McWhirter
 */
public interface ResourceContext {
    
    /** Retrieve the Vertx.
     * 
     * @return The Vertx.
     */
    Vertx vertx();
    
    /** Retrieve the container.
     * 
     * @return The container.
     */
    Container container();
    
    /** Retrieve the controller config.
     * 
     * @return The controller config.
     */
    Config config();

}
