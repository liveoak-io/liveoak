package io.liveoak.container.resource.mapping;

import java.util.HashMap;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.mapper.MappingExporter;
import io.liveoak.spi.resource.mapper.MappingResource;
import io.liveoak.spi.resource.mapper.Property;

/**
 * @author Ken Finnigan
 */
public class InMemoryConfigResourceMultiConvert implements RootResource, MappingResource {

    private Resource parent;
    String id;

    public InMemoryConfigResourceMultiConvert(String id) {
        this.id = id;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    private Thing thing;

    private void importConfig(@Property("firstValue") String firstValue, @Property("secondValue") String otherValue) throws Exception {
        thing = new Thing(firstValue, otherValue);
    }

    @MappingExporter
    public void exportConfig(HashMap<String, Object> config) throws Exception {
        config.put("firstValue", thing.getVal1());
        config.put("secondValue", thing.getVal2());
    }

    @Override
    public String id() {
        return id;
    }

    public static class Thing {
        private String val1;
        private String val2;

        public Thing(String val1, String val2) {
            this.val1 = val1;
            this.val2 = val2;
        }

        public String getVal1() {
            return val1;
        }

        public String getVal2() {
            return val2;
        }
    }
}
