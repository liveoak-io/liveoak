package io.liveoak.container.extension;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.util.function.Consumer;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class InitializeResourceService implements Service<Void> {

    public InitializeResourceService() {
    }

    public InitializeResourceService(Consumer<Exception> exceptionConsumer) {
        this.exceptionConsumer = exceptionConsumer;
    }

    @Override
    public void start(StartContext context) throws StartException {
        RequestContext reqContext = new RequestContext.Builder().build();
        context.asynchronous();
        try {
            this.resourceInjector.getValue().initializeProperties(reqContext, ConversionUtils.convert(this.configurationInjector.getValue()), new ServiceUpdateResponder(context));
        } catch (Exception e) {
            if (this.exceptionConsumer != null) {
                this.exceptionConsumer.accept(e);
                context.complete();
            } else {
                throw new StartException(e);
            }
        }
    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public Void getValue() throws IllegalStateException, IllegalArgumentException {
        return null;
    }

    public Injector<ObjectNode> configurationInjector() {
        return this.configurationInjector;
    }

    public Injector<RootResource> resourceInjector() {
        return this.resourceInjector;
    }

    private InjectedValue<ObjectNode> configurationInjector = new InjectedValue<>();
    private InjectedValue<RootResource> resourceInjector = new InjectedValue<>();
    private Consumer<Exception> exceptionConsumer;

}
