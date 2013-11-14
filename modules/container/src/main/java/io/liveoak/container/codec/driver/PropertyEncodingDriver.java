package io.liveoak.container.codec.driver;

import io.liveoak.spi.ReturnFields;

/**
 * @author Bob McWhirter
 */
public class PropertyEncodingDriver extends AbstractEncodingDriver {

    public PropertyEncodingDriver(EncodingDriver parent, String name, ReturnFields returnFields) {
        super(parent, null, returnFields );
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
        parent().encodeNext();
    }

    private String name;
}
