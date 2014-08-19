package io.liveoak.spi;

import java.io.File;
import java.util.Map;

/**
 * @author Bob McWhirter
 */
public interface Application {

    String id();
    String name();
    File directory();
    Boolean visible();
    Map<String, ApplicationClient> clients();
}
