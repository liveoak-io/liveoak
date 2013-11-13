package io.liveoak.filesystem;

import io.liveoak.spi.resource.async.Resource;
import org.vertx.java.core.Vertx;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public interface FSResource extends Resource {
    Vertx vertx();

    File file();
}
