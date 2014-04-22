package io.liveoak.security.policy.drools.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.liveoak.common.util.ObjectMapperFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JsonParsingUtils {

    private static ObjectMapper mapper = ObjectMapperFactory.create();

    public static Map<String, Object> parseJsonStringAsMap(String input) {
        if (input == null || input.isEmpty()) {
            return new HashMap<>();
        }

        try {
            return mapper.readValue(input, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
