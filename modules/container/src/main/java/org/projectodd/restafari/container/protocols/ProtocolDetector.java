package org.projectodd.restafari.container.protocols;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.projectodd.restafari.container.PipelineConfigurator;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Bob McWhirter
 */
public class ProtocolDetector extends ReplayingDecoder<Void> {

    private static final Charset UTF_8 = Charset.forName( "UTF-8" );

    public ProtocolDetector(PipelineConfigurator configurator) {
        this.configurator = configurator;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        int nonNewlineBytes = in.bytesBefore( (byte) '\n' );

        in.markReaderIndex();

        if (nonNewlineBytes > 0) {
            ByteBuf lineBuffer = in.readBytes( nonNewlineBytes );
            String line = lineBuffer.toString( UTF_8 );

            //SslHandler sslHandler = context.getPipeline().get( SslHandler.class );

            in.resetReaderIndex();
            ByteBuf fullBuffer = in.readBytes( super.actualReadableBytes() );

            if (line.startsWith( "CONNECT" ) || line.startsWith( "STOMP" )) {
                System.err.println( "switch to STOMP" );
                this.configurator.switchToPureStomp( ctx.pipeline() );
            } else {
                System.err.println( "switch to HTTP-based" );
                this.configurator.switchToHttpWebSockets( ctx.pipeline() );
            }

            ctx.pipeline().fireChannelRead( fullBuffer );
        }
    }

    private PipelineConfigurator configurator;

}


