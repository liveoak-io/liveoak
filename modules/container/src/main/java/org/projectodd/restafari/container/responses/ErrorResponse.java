package org.projectodd.restafari.container.responses;

import org.projectodd.restafari.container.requests.BaseRequest;

/**
 * @author Bob McWhirter
 */
public class ErrorResponse extends BaseResponse {

    public enum ResponseType {
        INTERNAL_ERROR,
        NOT_FOUND,
        NOT_ALLOWED,
        INVALID_MIME_TYPE,
    }

    public ErrorResponse(BaseRequest request, ResponseType responseType, String message) {
        super( request );
        this.responseType = responseType;
        this.message = message;
    }

    public ResponseType responseType() {
        return this.responseType;
    }

    public String message() {
        return this.message;
    }

    private ResponseType responseType;
    private String message;

}
