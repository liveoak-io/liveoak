package io.liveoak.common.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class ObjectMapperFactory {

    public static ObjectMapper create() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //mapper.configure( JsonGenerator.Feature.QUOTE_FIELD_NAMES, false );
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        return mapper;
    }

}
