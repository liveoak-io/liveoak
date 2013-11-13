package io.liveoak.container.codec.driver;

import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Bob McWhirter
 */
public class PropertiesEncodingDriver extends ResourceEncodingDriver {

    public PropertiesEncodingDriver(ResourceEncodingDriver parent, Resource resource) {
        super(parent, resource);
    }

    @Override
    public void encode() throws Exception {
        System.err.println( "properties::encode" );
        resource().readProperties( requestContext(), new MyPropertySink() );
    }

    @Override
    public void close() throws Exception {
        System.err.println( "properties::close" );
        parent().encodeNext();
    }

    private class MyPropertySink implements PropertySink {

        @Override
        public void accept(String name, Object value) {
            PropertyEncodingDriver propDriver = new PropertyEncodingDriver( PropertiesEncodingDriver.this, name );
            if ( value instanceof Resource ) {
                propDriver.addChildDriver(new ResourceEncodingDriver(propDriver, (Resource) value));
            } else if ( value instanceof List || value instanceof Set) {
                propDriver.addChildDriver( new ListEncodingDriver( propDriver, ((Collection)value).stream() ) );
            } else {
                propDriver.addChildDriver( new ValueEncodingDriver( propDriver, value ) );
            }
            addChildDriver( propDriver );
        }

        @Override
        public void close() {
            System.err.println( "prop sink close" );
            encodeNext();
        }
    }

}
