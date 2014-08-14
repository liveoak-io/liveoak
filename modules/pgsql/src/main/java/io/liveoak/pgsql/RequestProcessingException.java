package io.liveoak.pgsql;

import io.liveoak.spi.ResourceErrorResponse;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class RequestProcessingException extends RuntimeException {

    private final ResourceErrorResponse.ErrorType errorType;

    public RequestProcessingException(ResourceErrorResponse.ErrorType errorType) {
        this.errorType = errorType;
    }

    public RequestProcessingException(ResourceErrorResponse.ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public RequestProcessingException(ResourceErrorResponse.ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ResourceErrorResponse.ErrorType errorType() {
        return errorType;
    }
}