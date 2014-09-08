package io.liveoak.filesystem;

import io.liveoak.spi.Services;
import org.jboss.msc.service.ServiceName;

/**
 * @author Bob McWhirter
 */
public class FilesystemServices {

    public static final ServiceName FILESYSTEM = Services.LIVEOAK.append("filesystem");

    public static final ServiceName DIR = FILESYSTEM.append("dir");

    public static ServiceName directory(String appId, String id) {
        return DIR.append(appId, id);
    }
}
