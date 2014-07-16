package io.liveoak.spi.container.interceptor;

import java.io.IOException;
import java.util.UUID;

import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.state.ResourceState;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface InterceptorManager {

    void fireInbound(String chainName, ChannelHandlerContext ctx, ResourceRequest request);

    void fireOutbound(String chainName, ChannelHandlerContext ctx, ResourceResponse response);

    void fireComplete(String chainName, UUID requestId);

    void register(String interceptorName, Interceptor interceptor);

    void unregister(Interceptor interceptor);

    ResourceState getInterceptorsConfig() throws IOException;

    void setInterceptorsConfig(ResourceState resourceState) throws IOException;
}
