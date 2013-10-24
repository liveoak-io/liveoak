package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;
import org.projectodd.restafari.container.mime.MediaType;
import org.projectodd.restafari.container.mime.MediaTypeMatcher;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.HashMap;
import java.util.Map;

public class ResourceCodecManager {

    public void registerResourceCodec(String mediaType, ResourceCodec codec) {
        this.codecs.put(new MediaType( mediaType ), codec);
    }

    public ResourceState decode(MediaType mediaType, ByteBuf buf) throws Exception {
        System.err.println( "deocde with " + mediaType );
        ResourceCodec codec = this.codecs.get(mediaType);
        System.err.println( "codec: " + codec );
        System.err.println( this.codecs );
        return codec.decode(buf);
    }

    public ByteBuf encode(MediaType mediaType, Resource resource) throws Exception {
        ResourceCodec codec = this.codecs.get(mediaType);
        if ( codec == null ) {
            return encode( MediaType.JSON, resource );
        }
        return codec.encode(resource);
    }

    public ResourceCodec getResourceCodec(MediaType mediaType) {
        return this.codecs.get( mediaType );
    }

    public MediaType determineMediaType(String acceptMediaTypes) {
        if ( acceptMediaTypes == null ) {
            return MediaType.JSON;
        }
        MediaTypeMatcher matcher = new MediaTypeMatcher( acceptMediaTypes );

        MediaType match = matcher.findBestMatch( this.codecs.keySet() );

        return match;
    }

    private Map<MediaType, ResourceCodec> codecs = new HashMap<>();

}
