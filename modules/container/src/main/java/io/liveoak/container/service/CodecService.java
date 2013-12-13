package io.liveoak.container.service;

import io.liveoak.common.codec.Encoder;
import io.liveoak.common.codec.ResourceCodec;
import io.liveoak.common.codec.ResourceDecoder;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * @author Bob McWhirter
 */
public class CodecService implements Service<ResourceCodec> {

    public CodecService(Class<? extends Encoder> encoderClass, ResourceDecoder decoder) {
        this.codec = new ResourceCodec( encoderClass, decoder );
    }

    @Override
    public void start(StartContext context) throws StartException {
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public ResourceCodec getValue() throws IllegalStateException, IllegalArgumentException {
        return this.codec;
    }

    private ResourceCodec codec;
}
