package io.liveoak.container;

import java.io.File;
import java.util.HashMap;

import io.liveoak.spi.resource.config.ConfigProperty;
import io.liveoak.spi.resource.config.ConfigPropertyConverter;
import io.liveoak.spi.resource.config.ConfigMappingExporter;
import io.liveoak.spi.resource.config.Configurable;
import io.liveoak.spi.resource.RootResource;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
@Configurable
public class InMemoryConfigResourceWithConverter implements RootResource {

    String id;

    public InMemoryConfigResourceWithConverter(String id) {
        this.id = id;
    }

    @ConfigProperty(converter = FileConverter.class)
    private File file;

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

    public static class FileConverter implements ConfigPropertyConverter<File> {
        @Override
        public File createFrom(Object value) throws Exception {
            return new File(value.toString());
        }

        @Override
        public Object toConfigValue(File value) throws Exception {
            return value.getAbsolutePath();
        }
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
