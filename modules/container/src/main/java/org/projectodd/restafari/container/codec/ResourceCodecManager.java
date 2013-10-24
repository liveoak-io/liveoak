package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;
import org.projectodd.restafari.container.mime.MediaType;
import org.projectodd.restafari.container.mime.MediaTypeMatcher;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.*;
import java.util.stream.Collectors;

public class ResourceCodecManager {

    public void registerResourceCodec(String mediaType, ResourceCodec codec) {
        this.codecs.add(new CodecRegistration(new MediaType(mediaType), codec));
    }

    public ResourceState decode(MediaType mediaType, ByteBuf buf) throws Exception {
        ResourceCodec codec = getResourceCodec(mediaType);
        return codec.decode(buf);
    }

    public ByteBuf encode(MediaType mediaType, Resource resource) throws Exception {
        ResourceCodec codec = getResourceCodec(mediaType);
        if (codec == null) {
            return encode(MediaType.JSON, resource);
        }
        return codec.encode(resource);
    }

    public ResourceCodec getResourceCodec(MediaType mediaType) {
        for (CodecRegistration each : this.codecs) {
            if (each.mediaType.equals(mediaType)) {
                return each.codec;
            }
        }
        return null;
    }


    public MediaType determineMediaType(String acceptMediaTypes) {
        if (acceptMediaTypes == null) {
            return MediaType.JSON;
        }
        MediaTypeMatcher matcher = new MediaTypeMatcher(acceptMediaTypes);

        MediaType match = matcher.findBestMatch(this.codecs.stream().map((e) -> {
            return e.mediaType;
        }).collect(Collectors.toList()));

        return match;
    }

    private List<CodecRegistration> codecs = new ArrayList<>();

    private static class CodecRegistration {

        private MediaType mediaType;
        private ResourceCodec codec;

        CodecRegistration(MediaType mediaType, ResourceCodec codec) {
            this.mediaType = mediaType;
            this.codec = codec;

        }
    }

}
