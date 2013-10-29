package org.projectodd.restafari.spi.resource.async;

import org.projectodd.restafari.spi.resource.Resource;

/**
 * @author Bob McWhirter
 */
public class DelegatingResponder implements Responder {

    public DelegatingResponder(Responder delegate) {
        this.delegate = delegate;
    }

    @Override
    public void resourceRead(Resource resource) {
        delegate.resourceRead( resource );
    }

    @Override
    public void resourceCreated(Resource resource) {
        delegate.resourceCreated( resource );
    }

    @Override
    public void resourceDeleted(Resource resource) {
        delegate.resourceDeleted( resource );
    }

    @Override
    public void resourceUpdated(Resource resource) {
        delegate.resourceUpdated( resource );
    }

    @Override
    public void createNotSupported(Resource resource) {
        delegate.createNotSupported( resource );
    }

    @Override
    public void readNotSupported(Resource resource) {
        delegate.readNotSupported( resource );
    }

    @Override
    public void updateNotSupported(Resource resource) {
        delegate.updateNotSupported( resource );
    }

    @Override
    public void deleteNotSupported(Resource resource) {
        delegate.deleteNotSupported( resource );
    }

    @Override
    public void noSuchResource(String id) {
        delegate.noSuchResource( id );
    }

    @Override
    public void internalError(String message) {
        delegate.internalError( message );
    }

    private Responder delegate;
}
