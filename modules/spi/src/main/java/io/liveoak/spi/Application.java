package io.liveoak.spi;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public interface Application {

    String id();
    String name();
    File directory();

}
