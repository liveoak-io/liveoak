package io.liveoak.container;

import java.util.HashMap;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.config.ConfigMappingExporter;
import io.liveoak.spi.resource.config.ConfigProperty;
import io.liveoak.spi.resource.config.Configurable;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
@Configurable
public class InMemoryConfigResourceMultiConvert implements RootResource {

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

    private void importConfig(@ConfigProperty("firstValue") String firstValue, @ConfigProperty("secondValue") String otherValue) throws Exception {
        thing = new Thing(firstValue, otherValue);
    }

    @ConfigMappingExporter
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
