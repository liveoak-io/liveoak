package io.liveoak.container.deploy.service;

import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;

/**
 * @author Bob McWhirter
 */
public class ServiceResponder implements Responder {

    public ServiceResponder(StartContext context) {
        this.context = context;
    }

    @Override
    public void resourceRead(Resource resource) {
        context.complete();
    }

    @Override
    public void resourceCreated(Resource resource) {
        context.complete();
    }

    @Override
    public void resourceDeleted(Resource resource) {
        context.complete();
    }

    @Override
    public void resourceUpdated(Resource resource) {
        context.complete();
    }

    @Override
    public void createNotSupported(Resource resource) {
        context.failed(new StartException("create not supported"));
    }

    @Override
    public void readNotSupported(Resource resource) {
        context.failed(new StartException("read not supported"));
    }

    @Override
    public void updateNotSupported(Resource resource) {
        context.failed(new StartException("update not supported"));
    }

    @Override
    public void deleteNotSupported(Resource resource) {
        context.failed(new StartException("delete not supported"));
    }

    @Override
    public void noSuchResource(String id) {
        context.failed(new StartException("no such resource"));
    }

    @Override
    public void resourceAlreadyExists(String id) {
        context.failed(new StartException("resource already exists"));
    }

    @Override
    public void internalError(String message) {
        context.failed(new StartException(message));
    }

    @Override
    public void internalError(Throwable cause) {
        context.failed(new StartException(cause));
    }

    @Override
    public void invalidRequest(String message) {
        context.failed(new StartException(message));
    }

    @Override
    public void invalidRequest( Throwable cause ) {
        context.failed(new StartException(cause));
    }

    @Override
    public void invalidRequest( String message, Throwable cause ) {
        context.failed(new StartException(message, cause));
    }

    private final StartContext context;

}
