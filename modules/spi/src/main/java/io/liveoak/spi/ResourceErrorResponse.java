package io.liveoak.spi;

/**
 * @author Bob McWhirter
 */
public interface ResourceErrorResponse extends ResourceResponse {

    public enum ErrorType {
        NOT_AUTHORIZED,
        NOT_ACCEPTABLE,
        NO_SUCH_RESOURCE,
        RESOURCE_ALREADY_EXISTS,
        CREATE_NOT_SUPPORTED,
        READ_NOT_SUPPORTED,
        UPDATE_NOT_SUPPORTED,
        DELETE_NOT_SUPPORTED,
        INTERNAL_ERROR
    }

    ErrorType errorType();

    Throwable cause();

    String message();
}
