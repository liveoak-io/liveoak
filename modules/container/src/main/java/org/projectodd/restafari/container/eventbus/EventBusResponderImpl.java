package org.projectodd.restafari.container.eventbus;

import io.netty.buffer.ByteBuf;
import org.projectodd.restafari.container.codec.ResourceCodec;
import org.projectodd.restafari.container.codec.ResourceCodecManager;
import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.Responder;
import org.vertx.java.core.eventbus.Message;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;

public class EventBusResponderImpl implements Responder {

    public EventBusResponderImpl(String mimeType, Message message, ResourceCodecManager codecManager) {
        this.mimeType = mimeType;
        this.message = message;
        this.codecManager = codecManager;
    }
    @Override
    public void resource(Resource resource) {
        ResourceCodec codec = this.codecManager.getResourceCodec(this.mimeType);
        try {
            ByteBuf encoded = codec.encode(resource);
            message.reply(encoded.toString(Charset.forName("utf-8")));
        } catch (IOException e) {
            internalError(e.getMessage());
        }
    }

    @Override
    public void resources(Collection<Resource> resources) {
        ResourceCodec codec = this.codecManager.getResourceCodec(this.mimeType);
        try {
            ByteBuf encoded = codec.encode(resources);
            message.reply(encoded.toString(Charset.forName("utf-8")));
        } catch (IOException e) {
            internalError(e.getMessage());
        }
    }

    @Override
    public void resourceCreated(Resource resource) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resourceUpdated(Resource resource) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resourceDeleted(Resource resource) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void noSuchCollection(String name) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void noSuchResource(String id) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void internalError(String message) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private String mimeType;
    private Message message;
    private ResourceCodecManager codecManager;
}
