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
    private static final String RESOURCE_ALREADY_EXISTS = "RESOURCE_ALREADY_EXISTS";
    private static final String NO_SUCH_RESOURCE = "NO_SUCH_RESOURCE";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private JsonNode json;

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

    public HttpResponseAssert hasNoSuchResource() throws Exception {
        return validateErrorType(NO_SUCH_RESOURCE);
    }

    public HttpResponseAssert isDuplicate() throws Exception {
        return validateErrorType(RESOURCE_ALREADY_EXISTS);
    }

    public HttpResponseAssert isNotAcceptable() throws Exception {
        return validateErrorType(NOT_ACCEPTABLE);
    }

    public HttpResponseAssert isInternalError() throws Exception {
        return validateErrorType(INTERNAL_ERROR);
    }

    public HttpResponseAssert with(String message) throws Exception {
        isNotNull();
        readJson();
        String errorMsg = json.get("message").textValue();
        if (errorMsg.equals(message)) return this;
        failIfCustomMessageIsSet();
        throw failure(format("response error - expected error message:<%s> but was:<%s>", message, errorMsg));
    }

    private HttpResponseAssert validateErrorType(String expected) throws Exception {
        isNotNull();
        readJson();
        String errorType = json.get("error-type").textValue();
        if (errorType.equals(expected)) return this;
        failIfCustomMessageIsSet();
        throw failure(format("response error - expected error type:<%s> but was:<%s>", expected, errorType));
    }

    private void readJson() throws Exception {
        if (json == null) {
            json = ObjectMapperFactory.create().readTree(actual.getEntity().getContent());
        }
    }
}
