package org.projectodd.restafari.container;

/**
 * @author Bob McWhirter
 */
public class ResourceErrorResponse extends ResourceResponse {

    public enum ErrorType {
        NOT_AUTHORIZED,
        NOT_ACCEPTABLE,
        NO_SUCH_RESOURCE,
        CREATE_NOT_SUPPORTED,
        READ_NOT_SUPPORTED,
        UPDATE_NOT_SUPPORTED,
        DELETE_NOT_SUPPORTED,
    }

    public ResourceErrorResponse(ResourceRequest inReplyTo, ErrorType errorType) {
        super(inReplyTo, ResponseType.ERROR );
        this.errorType = errorType;
    }

    public ErrorType errorType() {
        return this.errorType;
    }

    private ErrorType errorType;
}
