package io.liveoak.container.extension.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.container.extension.ServiceUpdateResponder;
import io.liveoak.spi.Application;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
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
        this.async = false;
    }

    public InitializeResourceService(Consumer<Exception> exceptionConsumer) {
        this.exceptionConsumer = exceptionConsumer;
        this.async = true;
    }

    @Override
    public void start(StartContext context) throws StartException {
        RequestContext reqContext = new RequestContext.Builder().application(applicationInjector.getOptionalValue());

        context.asynchronous();

        if (async) {
            try {
                new Thread(() -> {
                    try {
                        this.resourceInjector.getValue().initializeProperties(reqContext, ConversionUtils.convert(this.configurationInjector.getValue()), new ServiceUpdateResponder(context));
                    } catch (Exception e) {
                        if (this.exceptionConsumer != null) {
                            this.exceptionConsumer.accept(e);
                            context.complete();
                        } else {
                            context.failed(new StartException(e));
                        }
                    }
                }, "InitializeResourceService starter - " + this.resourceInjector.getValue().id()).start();
            } catch (Throwable t) {
                context.failed(new StartException(t));
            }
        } else {
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

    public Injector<Application> applicationInjector() {
        return this.applicationInjector;
    }

    private InjectedValue<ObjectNode> configurationInjector = new InjectedValue<>();
    private InjectedValue<RootResource> resourceInjector = new InjectedValue<>();
    private InjectedValue<Application> applicationInjector = new InjectedValue<>();
    private Consumer<Exception> exceptionConsumer;
    private boolean async;

}
