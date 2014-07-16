package io.liveoak.container.service.bootstrap;

import io.liveoak.common.codec.ResourceCodec;
import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.common.codec.ResourceDecoder;
import io.liveoak.common.codec.StateEncoder;
import io.liveoak.common.codec.html.HTMLEncoder;
import io.liveoak.common.codec.json.JSONDecoder;
import io.liveoak.common.codec.json.JSONEncoder;
import io.liveoak.container.service.CodecInstallationService;
import io.liveoak.container.service.CodecManagerService;
import io.liveoak.container.service.CodecService;
import io.liveoak.spi.MediaType;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

import static io.liveoak.spi.LiveOak.CODEC_MANAGER;
import static io.liveoak.spi.LiveOak.codec;

/**
 * @author Bob McWhirter
 */
public class CodecBootstrappingService implements Service<Void> {
    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();

        CodecManagerService codecManager = new CodecManagerService();
        target.addService(CODEC_MANAGER, codecManager)
                .install();

        installCodec(target, MediaType.JSON, JSONEncoder.class, new JSONDecoder());
        installCodec(target, MediaType.HTML, HTMLEncoder.class, null);
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    private void installCodec(ServiceTarget target, MediaType mediaType, Class<? extends StateEncoder> encoderClass, ResourceDecoder decoder) {
        ServiceName name = codec(mediaType.toString());

        CodecService codec = new CodecService(encoderClass, decoder);
        target.addService(name, codec)
                .install();

        CodecInstallationService installer = new CodecInstallationService(mediaType);
        target.addService(name.append("install"), installer)
                .addDependency(name, ResourceCodec.class, installer.codecInjector())
                .addDependency(CODEC_MANAGER, ResourceCodecManager.class, installer.codecManagerInjector())
                .install();
    }
}
