package io.liveoak.testtools.assertions;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import io.liveoak.spi.util.ObjectMapperFactory;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.fest.assertions.GenericAssert;

import static org.fest.assertions.Formatting.*;

/**
 * @author Ken Finnigan
 */
public class HttpEntityAssert extends GenericAssert<HttpEntityAssert, HttpEntity> {
    /**
     * Creates a new <code>HttpEntityAssert</code>.
     *
     * @param actual the actual value to verify.
     */
    protected HttpEntityAssert(HttpEntity actual) {
        super(HttpEntityAssert.class, actual);
    }

    public HttpEntityAssert matches(String expected) throws IOException {
        isNotNull();
        String entityString = EntityUtils.toString(actual);
        if (expected.equals(entityString)) return this;
        failIfCustomMessageIsSet();
        throw failure(format("entity content - expected:<%s> but was:<%s>", expected, entityString));
    }

    public HttpEntityAssert matches(JsonNode expected) throws IOException {
        isNotNull();
        JsonNode entityJson = ObjectMapperFactory.create().readTree(actual.getContent());
        if (expected.equals(entityJson)) return this;
        failIfCustomMessageIsSet();
        throw failure(format("entity content - expected:<%s> but was:<%s>", expected, entityJson));
    }
}
