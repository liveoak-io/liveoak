package io.liveoak.container.interceptor;

import java.util.ArrayList;
import java.util.List;

import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.interceptor.DefaultInterceptor;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;
import io.liveoak.spi.container.interceptor.OutboundInterceptorContext;

/**
 * @author Bob McWhirter
 */
public class MockInterceptor extends DefaultInterceptor {

    @Override
    public void onInbound(InboundInterceptorContext context) throws Exception {
        this.requests.add( context.request() );
        super.onInbound( context );
    }

    @Override
    public void onOutbound(OutboundInterceptorContext context) throws Exception {
        this.responses.add( context.response() );
        super.onOutbound(context);
    }

    public List<ResourceRequest> requests() {
        return this.requests;
    }

    public List<ResourceResponse> responses() {
        return this.responses;
    }

    protected List<ResourceRequest> requests = new ArrayList<>();
    protected List<ResourceResponse> responses = new ArrayList<>();
}
