package io.liveoak.testtools.assertions;

import org.apache.http.HttpResponse;
import org.fest.assertions.GenericAssert;

import static org.fest.assertions.Formatting.*;

/**
 * @author Ken Finnigan
 */
public class HttpResponseAssert extends GenericAssert<HttpResponseAssert, HttpResponse> {

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
}
