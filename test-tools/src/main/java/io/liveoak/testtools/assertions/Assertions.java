package io.liveoak.testtools.assertions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

/**
 * @author Ken Finnigan
 */
public class Assertions extends org.fest.assertions.Assertions {
    public static HttpResponseAssert assertThat(HttpResponse actual) {
        return new HttpResponseAssert(actual);
    }

    public static HttpEntityAssert assertThat(HttpEntity actual) {
        return new HttpEntityAssert(actual);
    }
}
