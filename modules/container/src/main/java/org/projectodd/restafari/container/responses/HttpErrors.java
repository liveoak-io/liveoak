package org.projectodd.restafari.container.responses;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class HttpErrors {

    public static HttpErrorResponse badRequest(String message) {
        return new HttpErrorResponse(400, message);
    }

    public static HttpErrorResponse notFound(String requestUri) {
        return new HttpErrorResponse(404); // Sometimes less is better, just a 404 for now
    }

    public static HttpErrorResponse methodNotAllowed(String... allowed) {
        Objects.requireNonNull(allowed);

        return new HttpErrorResponse(405, null, toMap(HttpHeaders.Names.ALLOW, allowed));
    }

    public static HttpErrorResponse notAcceptable(String... acceptable) {
        Objects.requireNonNull(acceptable);
        //TODO: Add acceptable mimeTypes to response
        return new HttpErrorResponse(406, null);
    }

    public static HttpErrorResponse unsupportedMediaType() {
        return new HttpErrorResponse(415);
    }

    public static HttpErrorResponse internalError(String message) {
        return new HttpErrorResponse(500, message);
    }

    private static Map<String, List<String>> toMap(String headerName, String[] headerValues) {
        List<String> values = new ArrayList<>(headerValues.length);
        Collections.addAll(values, headerValues);

        Map<String, List<String>> map = new HashMap<>(1);
        map.put(headerName, values);

        return map;
    }
}
