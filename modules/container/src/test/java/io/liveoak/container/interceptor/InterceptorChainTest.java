package io.liveoak.container.interceptor;

import java.util.ArrayList;
import java.util.List;

import io.liveoak.common.DefaultResourceErrorResponse;
import io.liveoak.common.DefaultResourceRequest;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.container.interceptor.InboundInterceptorContext;
import io.liveoak.spi.container.interceptor.Interceptor;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class InterceptorChainTest {

    @Test
    public void testInboundChainNoChanges() throws Exception {
        List<Interceptor> interceptors = new ArrayList<>();

        MockInterceptor interceptor1 = new MockInterceptor();
        MockInterceptor interceptor2 = new MockInterceptor();

        interceptors.add(interceptor1);
        interceptors.add(interceptor2);

        EmbeddedChannel channel = new EmbeddedChannel(
                new ChannelDuplexHandler() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        InterceptorChain chain = new InterceptorChain(ctx, interceptors, (ResourceRequest) msg);
                        chain.fireInbound();
                    }
                }
        );

        ResourceRequest request = new DefaultResourceRequest.Builder(RequestType.READ, new ResourcePath("/foo/bar")).build();
        channel.writeInbound(request);

        ResourceRequest endRequest = (ResourceRequest) channel.readInbound();

        assertThat(endRequest).isNotNull();
        assertThat(interceptor1.requests()).hasSize(1);
        assertThat(interceptor1.requests()).contains(request);
        assertThat(interceptor2.requests()).hasSize(1);
        assertThat(interceptor2.requests()).contains(request);
    }

    @Test
    public void testInboundChainShortCircuit() throws Exception {

        List<Interceptor> interceptors = new ArrayList<>();

        ResourceRequest request = new DefaultResourceRequest.Builder(RequestType.READ, new ResourcePath("/foo/bar")).build();
        ResourceResponse response = new DefaultResourceErrorResponse(request, ResourceErrorResponse.ErrorType.NOT_AUTHORIZED);

        MockInterceptor interceptor1 = new MockInterceptor();
        MockInterceptor interceptor2 = new MockInterceptor() {
            @Override
            public void onInbound(InboundInterceptorContext context) throws Exception {
                requests.add(context.request());
                context.replyWith(response);
            }
        };
        MockInterceptor interceptor3 = new MockInterceptor();

        interceptors.add(interceptor1);
        interceptors.add(interceptor2);
        interceptors.add(interceptor3);

        EmbeddedChannel channel = new EmbeddedChannel(
                new ChannelDuplexHandler() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        InterceptorChain chain = new InterceptorChain(ctx, interceptors, (ResourceRequest) msg);
                        chain.fireInbound();
                    }
                }
        );

        channel.writeInbound(request);
        ResourceRequest endRequest = (ResourceRequest) channel.readInbound();
        Object endResponse = channel.readOutbound();

        assertThat(endRequest).isNull();

        assertThat(interceptor1.requests()).hasSize(1);
        assertThat(interceptor1.requests()).contains(request);
        assertThat(interceptor2.requests()).hasSize(1);
        assertThat(interceptor2.requests()).contains(request);
        assertThat(interceptor3.requests()).isEmpty();

        assertThat(interceptor3.responses()).isEmpty();
        assertThat(interceptor2.responses()).isEmpty();
        assertThat(interceptor1.responses()).hasSize(1);
        assertThat(interceptor1.responses()).contains(response);
    }

    @Test
    public void testOutbound() throws Exception {
        List<Interceptor> interceptors = new ArrayList<>();

        MockInterceptor interceptor1 = new MockInterceptor();
        MockInterceptor interceptor2 = new MockInterceptor();

        interceptors.add(interceptor1);
        interceptors.add(interceptor2);

        ResourceRequest request = new DefaultResourceRequest.Builder(RequestType.READ, new ResourcePath("/foo/bar")).build();
        ResourceResponse response = new DefaultResourceErrorResponse(request, ResourceErrorResponse.ErrorType.NOT_AUTHORIZED);

        EmbeddedChannel channel = new EmbeddedChannel(
                new ChannelDuplexHandler() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        InterceptorChain chain = new InterceptorChain(ctx, interceptors, (ResourceResponse) msg);
                        chain.fireOutbound();
                    }
                }
        );

        channel.writeInbound(request);
        ResourceRequest endRequest = (ResourceRequest) channel.readInbound();

        channel.writeAndFlush(response);
        ResourceResponse endResponse = (ResourceResponse) channel.readOutbound();

        assertThat(endResponse).isNotNull();
        assertThat(interceptor2.responses()).hasSize(1);
        assertThat(interceptor2.responses()).contains(response);
        assertThat(interceptor1.responses()).hasSize(1);
        assertThat(interceptor1.responses()).contains(response);
    }
}
