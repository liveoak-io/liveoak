/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.container.codec;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.BinaryContentSink;
import io.liveoak.spi.resource.async.BinaryResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Bob McWhirter
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class ResourceCodecManager {

    public void registerResourceCodec( String mediaType, ResourceCodec codec ) {
        this.codecs.add( new CodecRegistration( new MediaType( mediaType ), codec ) );
    }

    public ResourceState decode( MediaType mediaType, ByteBuf buf ) throws Exception {
        ResourceCodec codec = getResourceCodec( mediaType );
        if (codec == null || !codec.hasDecoder()) {
            throw new UnsupportedMediaTypeException( Collections.singletonList( mediaType ) );
        }
        return codec.decode( buf );
    }

    public EncodingResult encode( RequestContext ctx, MediaTypeMatcher mediaTypeMatcher, Resource resource ) throws Exception {

        if ( resource instanceof BinaryResource ) {
            MediaType match = mediaTypeMatcher.findBestMatch( Collections.singletonList( ( ( BinaryResource ) resource ).mediaType() ) );
            ;
            if ( match != null ) {
                CompletableFuture<ByteBuf> future = new CompletableFuture<>();
                ( ( BinaryResource ) resource ).readContent( ctx, new MyBinaryContentSink( future ) );
                return new EncodingResult( match, future.get() );
            } else {
                throw new IncompatibleMediaTypeException( mediaTypeMatcher.mediaTypes(), ( BinaryResource ) resource );
            }
        }

        MediaType bestMatch = mediaTypeMatcher.findBestMatch( this.codecs.stream().map( ( e ) -> {
            return e.mediaType;
        } ).collect( Collectors.toList() ) );

        if ( bestMatch == null ) {
            throw new UnsupportedMediaTypeException( mediaTypeMatcher.mediaTypes() );
        }

        ResourceCodec codec = getResourceCodec( bestMatch );

        if ( codec == null ) {
            codec = getResourceCodec( MediaType.JSON );
            bestMatch = MediaType.JSON;
        }

        if (!codec.hasEncoder()) {
            throw new UnsupportedMediaTypeException(mediaTypeMatcher.mediaTypes());
        }

        return new EncodingResult( bestMatch, codec.encode( ctx, resource ) );
    }

    public ResourceCodec getResourceCodec( MediaType mediaType ) {
        for ( CodecRegistration each : this.codecs ) {
            if ( each.mediaType.equals( mediaType ) ) {
                return each.codec;
            }
        }
        return null;
    }


    public MediaType determineMediaType( String acceptMediaTypes, String extension ) {
        if ( acceptMediaTypes == null && extension == null ) {
            return MediaType.JSON;
        }

        MediaTypeMatcher matcher = new MediaTypeMatcher( acceptMediaTypes, extension );

        MediaType match = matcher.findBestMatch( this.codecs.stream().map( ( e ) -> {
            return e.mediaType;
        } ).collect( Collectors.toList() ) );

        return match;
    }

    private List<CodecRegistration> codecs = new ArrayList<>();

    private static class CodecRegistration {

        private MediaType mediaType;
        private ResourceCodec codec;

        CodecRegistration( MediaType mediaType, ResourceCodec codec ) {
            this.mediaType = mediaType;
            this.codec = codec;

        }
    }

    private static class MyBinaryContentSink implements BinaryContentSink {

        private ByteBuf buffer = Unpooled.buffer();
        private CompletableFuture<ByteBuf> future;

        MyBinaryContentSink( CompletableFuture<ByteBuf> future ) {
            this.future = future;
        }

        @Override
        public void close() {
            future.complete( this.buffer );
        }

        @Override
        public void accept( ByteBuf byteBuf ) {
            buffer.writeBytes( byteBuf );
        }
    }

}
