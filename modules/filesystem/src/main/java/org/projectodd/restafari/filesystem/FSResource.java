package org.projectodd.restafari.filesystem;

import org.projectodd.restafari.spi.resource.async.Resource;
import org.vertx.java.core.Vertx;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public interface FSResource extends Resource {
    Vertx vertx();

    File file();
}
