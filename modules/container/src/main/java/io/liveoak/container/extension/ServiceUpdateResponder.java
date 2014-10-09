package io.liveoak.container.extension;

import io.liveoak.spi.exceptions.ResourceAlreadyExistsException;
import io.liveoak.spi.ResourceErrorResponse;
import io.liveoak.spi.exceptions.ResourceNotFoundException;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;

/**
 * @author Bob McWhirter
 */
public class ServiceUpdateResponder implements Responder {

    private final StartContext context;

    public ServiceUpdateResponder(StartContext context) {
        this.context = context;
    }

    @Override
    public void resourceRead(Resource resource) {
        this.context.complete();
    }

    @Override
    public void resourceCreated(Resource resource) {
        this.context.complete();
    }

    @Override
    public void resourceDeleted(Resource resource) {
        this.context.complete();
    }

    @Override
    public void resourceUpdated(Resource resource) {
        this.context.complete();
    }

    @Override
    public void createNotSupported(Resource resource) {
        this.context.failed(new StartException("Create not supported"));
    }

    @Override
    public void readNotSupported(Resource resource) {
        this.context.failed(new StartException("Read not supported"));

    }

    @Override
    public void updateNotSupported(Resource resource) {
        this.context.failed(new StartException("Update not supported"));

    }

    @Override
    public void deleteNotSupported(Resource resource) {
        this.context.failed(new StartException("Delete not supported"));

    }

    @Override
    public void noSuchResource(String id) {
        this.context.failed(new StartException(new ResourceNotFoundException(id)));
    }

    @Override
    public void resourceAlreadyExists(String id) {
        this.context.failed(new StartException(new ResourceAlreadyExistsException(id)));
    }

    @Override
    public void internalError(String message) {
        this.context.failed(new StartException(message));
    }

    @Override
    public void internalError(String message, Throwable cause) {
        this.context.failed(new StartException(message, cause));
    }

    @Override
    public void internalError(Throwable cause) {
        this.context.failed(new StartException(cause));
    }

    @Override
    public void invalidRequest(String message) {
        this.context.failed(new StartException(message));
    }

    @Override
    public void invalidRequest(Throwable cause) {
        this.context.failed(new StartException(cause));

    }

    @Override
    public void invalidRequest(String message, Throwable cause) {
        this.context.failed(new StartException(message, cause));

    }

    @Override
    public void error(ResourceErrorResponse.ErrorType errorType) {
        this.context.failed(new StartException(errorType.toString()));
    }

    @Override
    public void error(ResourceErrorResponse.ErrorType errorType, String message) {
        this.context.failed(new StartException(message));
    }

    @Override
    public void error(ResourceErrorResponse.ErrorType errorType, String message, Throwable cause) {
        this.context.failed(new StartException(cause));
    }
}
