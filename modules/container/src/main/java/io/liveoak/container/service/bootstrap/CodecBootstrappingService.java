package io.liveoak.container.service.bootstrap;

import java.util.ArrayList;
import java.util.List;

import io.liveoak.common.codec.ResourceCodec;
import io.liveoak.common.codec.ResourceCodecManager;
import io.liveoak.common.codec.ResourceDecoder;
import io.liveoak.common.codec.StateEncoder;
import io.liveoak.common.codec.form.FormURLDecoder;
import io.liveoak.common.codec.html.HTMLEncoder;
import io.liveoak.common.codec.json.JSONDecoder;
import io.liveoak.common.codec.json.JSONEncoder;
import io.liveoak.container.service.CodecInstallationCompleteService;
import io.liveoak.container.service.CodecInstallationService;
import io.liveoak.container.service.CodecManagerService;
import io.liveoak.container.service.CodecService;
import io.liveoak.spi.MediaType;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

import static io.liveoak.spi.Services.CODEC_MANAGER;
import static io.liveoak.spi.Services.CODEC_MANAGER_COMPLETE;
import static io.liveoak.spi.Services.codec;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class CodecBootstrappingService implements Service<Void> {
    @Override
    public void start(StartContext context) throws StartException {
        ServiceTarget target = context.getChildTarget();

        CodecManagerService codecManager = new CodecManagerService();
        target.addService(CODEC_MANAGER, codecManager)
                .install();

        List<ServiceName> codecCompletionDependencies = new ArrayList<>();

        codecCompletionDependencies.add(installCodec(target, MediaType.JSON, JSONEncoder.class, new JSONDecoder()));
        codecCompletionDependencies.add(installCodec(target, MediaType.HTML, HTMLEncoder.class, null));
        codecCompletionDependencies.add(installCodec(target, MediaType.FORM_URLENCODED, null, new FormURLDecoder()));

        // Custom Media Types
        codecCompletionDependencies.add(installCodec(target, MediaType.LOCAL_APP_JSON, JSONEncoder.class, new JSONDecoder()));

        // Install completion service
        ServiceBuilder<Void> builder = target.addService(CODEC_MANAGER_COMPLETE, new CodecInstallationCompleteService());
        codecCompletionDependencies.forEach(each -> builder.addDependency(each));
        builder.install();
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    private ServiceName installCodec(ServiceTarget target, MediaType mediaType, Class<? extends StateEncoder> encoderClass, ResourceDecoder decoder) {
        ServiceName name = codec(mediaType.toString());

        CodecService codec = new CodecService(encoderClass, decoder);
        target.addService(name, codec)
                .install();

        CodecInstallationService installer = new CodecInstallationService(mediaType);
        target.addService(name.append("install"), installer)
                .addDependency(name, ResourceCodec.class, installer.codecInjector())
                .addDependency(CODEC_MANAGER, ResourceCodecManager.class, installer.codecManagerInjector())
                .install();

        return name.append("install");
    }
}
