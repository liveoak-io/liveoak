package io.liveoak.container.codec.driver;

import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class ValueEncodingDriver extends AbstractEncodingDriver {

    public ValueEncodingDriver(EncodingDriver parent, Object object) {
        super(parent, object, null);
    }

    @Override
    public void encode() throws Exception {
        Object o = object();

        if ( o instanceof String ) {
            encoder().writeValue((String) o);
        } else if ( o instanceof Integer ) {
            encoder().writeValue((Integer) o);
        } else if ( o instanceof Double ) {
            encoder().writeValue((Double) o);
        } else if ( o instanceof Resource) {
            encoder().writeLink((Resource) o);
        } else {
        }
        close();
    }

    @Override
    public void close() throws Exception {
        parent().encodeNext();
    }
}
