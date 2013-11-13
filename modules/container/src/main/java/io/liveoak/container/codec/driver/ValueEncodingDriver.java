package io.liveoak.container.codec.driver;

/**
 * @author Bob McWhirter
 */
public class ValueEncodingDriver extends AbstractEncodingDriver {

    public ValueEncodingDriver(EncodingDriver parent, Object object) {
        super(parent, object);
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
        } else {
            System.err.println( "WHAT? " + o.getClass() );
        }
        close();
    }

    @Override
    public void close() throws Exception {
        parent().encodeNext();
    }
}
