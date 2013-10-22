package org.projectodd.restafari.filesystem;

import org.projectodd.restafari.spi.resource.Resource;
import org.vertx.java.core.Vertx;

/**
 * @author Bob McWhirter
 */
public interface FSResource extends Resource {
    Vertx vertx();
}
