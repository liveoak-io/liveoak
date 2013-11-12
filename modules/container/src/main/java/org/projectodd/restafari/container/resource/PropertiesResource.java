package org.projectodd.restafari.container.resource;

import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.async.PropertySink;
import org.projectodd.restafari.spi.resource.async.Resource;

import java.util.Properties;

/**
 * @author Bob McWhirter
 */
public class PropertiesResource implements Resource {

    public PropertiesResource(ContainerResource parent) {
        this.parent = parent;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) {
        Properties allProps = System.getProperties();
        for ( String key : allProps.stringPropertyNames() ) {
            sink.accept( key, allProps.getProperty( key ) );
        }

            sink.close();
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return "properties";
    }

    private ContainerResource parent;
}
