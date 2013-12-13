package io.liveoak.container;

import java.io.File;

import io.liveoak.spi.resource.config.ConfigMapping;
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

    @ConfigMapping(properties = {@ConfigProperty("firstValue"), @ConfigProperty("secondValue")}, importMethod = "importConfig")
    private Thing thing;

    private void importConfig(Object... configValues) throws Exception {
        thing = new Thing((String)configValues[0], (String)configValues[1]);
    }

    @ConfigMappingExporter
    public Object firstValue() throws Exception {
        return thing.getVal1();
    }

    @ConfigMappingExporter("secondValue")
    public Object convert() throws Exception {
        return thing.getVal2();
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
