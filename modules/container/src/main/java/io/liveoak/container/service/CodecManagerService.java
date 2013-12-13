package io.liveoak.container.service;

import io.liveoak.common.codec.ResourceCodecManager;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class CodecManagerService implements Service<ResourceCodecManager> {

    public CodecManagerService() {
        this.codecManager = new ResourceCodecManager();
    }

    @Override
    public void start(StartContext context) throws StartException {
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public ResourceCodecManager getValue() throws IllegalStateException, IllegalArgumentException {
        return this.codecManager;
    }

    private ResourceCodecManager codecManager;

}
