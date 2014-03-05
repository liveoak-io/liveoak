package io.liveoak.interceptor.test;

import java.util.concurrent.atomic.AtomicInteger;

import io.liveoak.common.DefaultResourceResponse;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.interceptor.DefaultInterceptor;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;
import io.liveoak.spi.container.interceptor.OutboundInterceptorContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MockCounterInterceptor extends DefaultInterceptor {

    private static final AtomicInteger requestCounter = new AtomicInteger();
    private static final AtomicInteger responseCounter = new AtomicInteger();

    @Override
    public void onInbound(InboundInterceptorContext context) throws Exception {
        if (context.request().requestType() == RequestType.CREATE) {
            ResourceResponse response = new DefaultResourceResponse(context.request(), ResourceResponse.ResponseType.CREATED, new Resource() {

                @Override
                public Resource parent() {
                    return null;
                }

                @Override
                public String id() {
                    return "mock" ;
                }

            });

            ResourceState state = new DefaultResourceState();
            state.putProperty("requestCounter", requestCounter.get());
            state.putProperty("responseCounter", responseCounter.get());
            response.setState(state);

            context.replyWith(response);
        } else {
            requestCounter.incrementAndGet();
            super.onInbound(context);
        }
    }

    @Override
    public void onOutbound(OutboundInterceptorContext context) throws Exception {
        responseCounter.incrementAndGet();
        super.onOutbound(context);
    }
}
