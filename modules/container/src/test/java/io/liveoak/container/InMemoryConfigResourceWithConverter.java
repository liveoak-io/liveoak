package io.liveoak.container;

import java.io.File;

import io.liveoak.spi.resource.config.ConfigProperty;
import io.liveoak.spi.resource.config.ConfigPropertyConverter;
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
}
