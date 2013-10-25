package org.projectodd.restafari.container.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.projectodd.restafari.spi.MediaType;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.async.BinaryContentSink;
import org.projectodd.restafari.spi.resource.async.BinaryResource;
import org.projectodd.restafari.spi.state.ResourceState;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ResourceCodecManager {

    public void registerResourceCodec(String mediaType, ResourceCodec codec) {
        this.codecs.add(new CodecRegistration(new MediaType(mediaType), codec));
    }

    public ResourceState decode(MediaType mediaType, ByteBuf buf) throws Exception {
        ResourceCodec codec = getResourceCodec(mediaType);
        if (codec == null) {
            throw new UnsupportedMediaTypeException(Collections.singletonList(mediaType));
        }
        return codec.decode(buf);
    }

    public EncodingResult encode(MediaTypeMatcher mediaTypeMatcher, Resource resource) throws Exception {

        if (resource instanceof BinaryResource) {
            MediaType match = mediaTypeMatcher.findBestMatch(Collections.singletonList(((BinaryResource) resource).mediaType()));
            ;
            if (match != null) {
                CompletableFuture<ByteBuf> future = new CompletableFuture<>();
                ((BinaryResource) resource).readContent(new MyBinaryContentSink(future));
                return new EncodingResult(match, future.get());
            } else {
                throw new IncompatibleMediaTypeException(mediaTypeMatcher.mediaTypes(), (BinaryResource) resource);
            }
        }

        MediaType bestMatch = mediaTypeMatcher.findBestMatch(this.codecs.stream().map((e) -> {
            return e.mediaType;
        }).collect(Collectors.toList()));

        if (bestMatch == null) {
            throw new UnsupportedMediaTypeException(mediaTypeMatcher.mediaTypes());
        }

        ResourceCodec codec = getResourceCodec(bestMatch);

        if (codec == null) {
            codec = getResourceCodec(MediaType.JSON);
            bestMatch = MediaType.JSON;
        }

        return new EncodingResult(bestMatch, codec.encode(resource));
    }

    public ResourceCodec getResourceCodec(MediaType mediaType) {
        for (CodecRegistration each : this.codecs) {
            if (each.mediaType.equals(mediaType)) {
                return each.codec;
            }
        }
        return null;
    }


    public MediaType determineMediaType(String acceptMediaTypes, String extension) {
        if (acceptMediaTypes == null && extension == null) {
            return MediaType.JSON;
        }

        MediaTypeMatcher matcher = new MediaTypeMatcher(acceptMediaTypes, extension);

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

    private static class MyBinaryContentSink implements BinaryContentSink {

        private ByteBuf buffer = Unpooled.buffer();
        private CompletableFuture<ByteBuf> future;

        MyBinaryContentSink(CompletableFuture<ByteBuf> future) {
            this.future = future;
        }

        @Override
        public void close() throws Exception {
            future.complete(this.buffer);
        }

        @Override
        public void accept(ByteBuf byteBuf) {
            buffer.writeBytes(byteBuf);
        }
    }

}
