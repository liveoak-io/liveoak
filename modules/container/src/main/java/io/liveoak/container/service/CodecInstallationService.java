package io.liveoak.container.service;

import io.liveoak.common.codec.ResourceCodec;
import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.spi.MediaType;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class CodecInstallationService implements Service<Void> {

    public CodecInstallationService(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.codecManagerInjector.getValue().registerResourceCodec( this.mediaType, this.codecInjector.getValue() );
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<ResourceCodec> codecInjector() {
        return this.codecInjector;
    }

    public Injector<ResourceCodecManager> codecManagerInjector() {
        return this.codecManagerInjector;
    }

    private InjectedValue<ResourceCodec> codecInjector = new InjectedValue<>();
    private InjectedValue<ResourceCodecManager> codecManagerInjector = new InjectedValue<>();

    private MediaType mediaType;
}
