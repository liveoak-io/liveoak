package io.liveoak.container.resource.mapping;

import java.io.File;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.mapper.MappingResource;
import io.liveoak.spi.resource.mapper.Property;
import io.liveoak.spi.resource.mapper.PropertyConverter;

/**
 * @author Ken Finnigan
 */
public class InMemoryConfigResourceWithConverter implements RootResource, MappingResource {

    private Resource parent;
    String id;

    @Property(converter = FileConverter.class)
    private File file;

    public InMemoryConfigResourceWithConverter(String id) {
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

    @Override
    public String id() {
        return id;
    }


    public static class FileConverter implements PropertyConverter<File> {
        @Override
        public File createFrom(Object value) throws Exception {
            return new File(value.toString());
        }

        @Override
        public Object toValue(File value) throws Exception {
            return value.getAbsolutePath();
        }
    }
}
