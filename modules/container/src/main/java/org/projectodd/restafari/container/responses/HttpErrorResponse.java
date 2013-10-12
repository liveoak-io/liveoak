package org.projectodd.restafari.container.responses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class HttpErrorResponse {

    private final int code;
    private final String message;
    private final Map<String, List<String>> headers;

    public HttpErrorResponse(int code) {
        this(code, null, new HashMap<>());
    }

    public HttpErrorResponse(int code, String message) {
        this(code, message, new HashMap<>());
    }

    public HttpErrorResponse(int code, String message, Map<String, List<String>> headers) {
        this.code = code;
        this.message = message;
        this.headers = headers;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }
}
