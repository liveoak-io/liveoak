package io.liveoak.testtools.assertions;

import com.fasterxml.jackson.databind.JsonNode;
import io.liveoak.common.util.ObjectMapperFactory;
import org.apache.http.HttpResponse;
import org.fest.assertions.GenericAssert;

import static org.fest.assertions.Formatting.*;

/**
 * @author Ken Finnigan
 */
public class HttpResponseAssert extends GenericAssert<HttpResponseAssert, HttpResponse> {
    private static final String NOT_ACCEPTABLE = "NOT_ACCEPTABLE";

    /**
     * Creates a new <code>HttpResponseAssert</code>.
     *
     * @param actual the actual value to verify.
     */
    protected HttpResponseAssert(HttpResponse actual) {
        super(HttpResponseAssert.class, actual);
    }

    public HttpResponseAssert hasStatus(int expected) {
        isNotNull();
        if (actual.getStatusLine().getStatusCode() == expected) return this;
        failIfCustomMessageIsSet();
        throw failure(format("response code - expected:<%s> but was:<%s>", expected, actual.getStatusLine().getStatusCode()));
    }

    public HttpResponseAssert isNotAcceptable(String message) throws Exception {
        isNotNull();
        JsonNode json = ObjectMapperFactory.create().readTree(actual.getEntity().getContent());
        String errorType = json.get("error-type").textValue();
        String errorMsg = json.get("message").textValue();
        if (errorType.equals(NOT_ACCEPTABLE) && errorMsg.equals(message)) return this;
        failIfCustomMessageIsSet();
        throw failure(format("response error - expected error type:<%s> but was:<%s> \n expected error message:<%s> but was:<%s>", NOT_ACCEPTABLE, errorType, message, errorMsg));
    }
}
