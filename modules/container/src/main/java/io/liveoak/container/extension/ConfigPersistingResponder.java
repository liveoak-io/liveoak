package io.liveoak.container.extension;

import io.liveoak.container.util.ConversionUtils;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

import java.io.IOException;

/**
 * @author Bob McWhirter
 */
public class ConfigPersistingResponder implements Responder {

    public ConfigPersistingResponder(AdminResourceWrappingResource resource, ResourceState state, Responder delegate) {
        this.resource = resource;
        this.state = state;
        this.delegate = delegate;
    }

    @Override
    public void resourceRead(Resource resource) {
        this.delegate.resourceRead( resource );
    }

    @Override
    public void resourceCreated(Resource resource) {
        this.delegate.resourceCreated( resource );
    }

    @Override
    public void resourceDeleted(Resource resource) {
        this.delegate.resourceDeleted( resource );
    }

    @Override
    public void resourceUpdated(Resource resource) {
        try {
            this.resource.configurationManager().updateResource( this.resource.id(), this.resource.type(), ConversionUtils.convert( this.state ));
            this.delegate.resourceUpdated(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createNotSupported(Resource resource) {
        this.delegate.createNotSupported( resource );
    }

    @Override
    public void readNotSupported(Resource resource) {
        this.delegate.readNotSupported( resource );
    }

    @Override
    public void updateNotSupported(Resource resource) {
        this.delegate.updateNotSupported( resource );
    }

    @Override
    public void deleteNotSupported(Resource resource) {
        this.delegate.deleteNotSupported( resource );
    }

    @Override
    public void noSuchResource(String id) {
        this.delegate.noSuchResource( id );
    }

    @Override
    public void resourceAlreadyExists(String id) {
        this.delegate.resourceAlreadyExists( id );
    }

    @Override
    public void internalError(String message) {
        this.delegate.internalError(message);
    }

    @Override
    public void internalError(Throwable cause) {
        this.delegate.internalError( cause );
    }

    @Override
    public void invalidRequest(String message) {
        this.delegate.invalidRequest( message );
    }

    @Override
    public void invalidRequest(Throwable cause) {
        this.delegate.invalidRequest( cause );
    }

    @Override
    public void invalidRequest(String message, Throwable cause) {
        this.delegate.invalidRequest( message, cause );
    }

    private final AdminResourceWrappingResource resource;
    private final ResourceState state;
    private Responder delegate;
}
