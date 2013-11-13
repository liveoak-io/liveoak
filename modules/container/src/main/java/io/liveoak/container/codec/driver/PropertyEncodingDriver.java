package io.liveoak.container.codec.driver;

/**
 * @author Bob McWhirter
 */
public class PropertyEncodingDriver extends AbstractEncodingDriver {

    public PropertyEncodingDriver(EncodingDriver parent, String name) {
        super(parent, null);
        this.name = name;
    }

    @Override
    public void encode() throws Exception {
        encoder().startProperty( this.name );
        encodeNext();
    }

    @Override
    public void close() throws Exception {
        encoder().endProperty( this.name );
        System.err.println( "single prop done, parent, encodeNext: " + parent() );
        parent().encodeNext();
    }

    private String name;
}
