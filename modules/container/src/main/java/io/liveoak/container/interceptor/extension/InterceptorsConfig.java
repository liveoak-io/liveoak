package io.liveoak.container.interceptor.extension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.spi.state.ResourceState;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class InterceptorsConfig {

    private Map<String, List<InterceptorConfigEntry>> interceptorChainConfigs = new HashMap<>();

    public void addChainConfig(String name, List<InterceptorConfigEntry> chainConfigEntry) {
        interceptorChainConfigs.put(name, chainConfigEntry);
    }

    public List<InterceptorConfigEntry> getChainConfig(String name) {
        List<InterceptorConfigEntry> chainConfig =  interceptorChainConfigs.get(name);
        return chainConfig!=null ? chainConfig : Collections.EMPTY_LIST;
    }

    public static InterceptorsConfig createConfigFromResourceState(ResourceState resourceState) throws IOException {
        ObjectNode config = ConversionUtils.convert(resourceState);
        InterceptorsConfig result = new InterceptorsConfig();
        ObjectMapper objectMapper = ObjectMapperFactory.create();

        Iterator<String> iterator = config.fieldNames();
        while (iterator.hasNext()) {
            String chainName = iterator.next();
            ArrayNode chainNode = (ArrayNode)config.get(chainName);

            List<InterceptorConfigEntry> chainConfig = new LinkedList<>();
            for (JsonNode jsonNode : chainNode) {
                ObjectNode interceptorConfig = (ObjectNode)jsonNode;
                InterceptorConfigEntry configEntry = objectMapper.readValue(interceptorConfig.toString(), InterceptorConfigEntry.class);
                chainConfig.add(configEntry);
            }
            result.addChainConfig(chainName, chainConfig);
        }
        return result;
    }

    public ResourceState getConfigAsResourceState() throws IOException {
        ObjectNode result = JsonNodeFactory.instance.objectNode();
        for (Map.Entry<String, List<InterceptorConfigEntry>> entry : interceptorChainConfigs.entrySet()) {
            String chainName = entry.getKey();
            List<InterceptorConfigEntry> chainConfigs = entry.getValue();
            ArrayNode chainsNode = result.putArray(chainName);

            for (InterceptorConfigEntry configEntry : chainConfigs) {
                ObjectMapper mapper = ObjectMapperFactory.create();
                String cfg = mapper.writeValueAsString(configEntry);
                ObjectNode interceptorConfigNode = mapper.readValue(cfg, ObjectNode.class);
                chainsNode.add(interceptorConfigNode);
            }
        }

        return ConversionUtils.convert(result);
    }

    @Override
    public String toString() {
        return "InterceptorsConfig: " + interceptorChainConfigs;
    }
}
