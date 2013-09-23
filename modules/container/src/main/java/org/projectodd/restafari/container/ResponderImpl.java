package org.projectodd.restafari.container;

import io.netty.channel.ChannelHandlerContext;

import java.util.Collection;

import org.projectodd.restafari.container.responses.NoSuchCollectionResponse;
import org.projectodd.restafari.container.responses.NoSuchResourceResponse;
import org.projectodd.restafari.container.responses.ResourceCreatedResponse;
import org.projectodd.restafari.container.responses.ResourceDeletedResponse;
import org.projectodd.restafari.container.responses.ResourceResponse;
import org.projectodd.restafari.container.responses.ResourceUpdatedResponse;
import org.projectodd.restafari.container.responses.ResourcesResponse;
import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.Responder;

public class ResponderImpl implements Responder {

    public ResponderImpl(String mimeType, ChannelHandlerContext ctx) {
        this.mimeType = mimeType;
        this.ctx = ctx;
    }
    
    @Override
    public void resource(Resource resource) {
        this.ctx.write( new ResourceResponse( this.mimeType, resource ) );
        this.ctx.flush();
    }

    @Override
    public void resources(Collection<Resource> resources) {
        this.ctx.write(new ResourcesResponse( this.mimeType, resources) );
        this.ctx.flush();
    }

    @Override
    public void resourceCreated(Resource resource) {
        this.ctx.write(new ResourceCreatedResponse( this.mimeType, resource ) );
        this.ctx.flush();
    }

    @Override
    public void resourceUpdated(Resource resource) {
        this.ctx.write( new ResourceUpdatedResponse( this.mimeType, resource ) );
        this.ctx.flush();
    }

    @Override
    public void resourceDeleted(Resource resource) {
        this.ctx.write( new ResourceDeletedResponse( this.mimeType, resource ) );
        this.ctx.flush();
    }

    @Override
    public void noSuchCollection(String name) {
        this.ctx.write( new NoSuchCollectionResponse( this.mimeType, name ) );
        this.ctx.flush();
    }

    @Override
    public void noSuchResource(String id) {
        this.ctx.write( new NoSuchResourceResponse( this.mimeType, id ) );
        this.ctx.flush();
    }

    @Override
    public void internalError(String message) {
        this.ctx.write( new InternalError(message));
        this.ctx.flush();
    }

    private String mimeType;
    private ChannelHandlerContext ctx;

}
