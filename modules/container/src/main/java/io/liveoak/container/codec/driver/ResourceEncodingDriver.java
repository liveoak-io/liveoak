package io.liveoak.container.codec.driver;

import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class ResourceEncodingDriver extends AbstractEncodingDriver {

    public ResourceEncodingDriver(Resource resource) {
        super(resource);
    }

    public ResourceEncodingDriver(EncodingDriver parent, Resource resource) {
        super(parent, resource);
    }

    public Resource resource() {
        return (Resource) object();
    }

    @Override
    public void encode() throws Exception {
        encoder().startResource( resource() );
        addChildDriver(new PropertiesEncodingDriver(this, resource()));
        addChildDriver( new MembersEncodingDriver( this, resource() ));
        encodeNext();
    }

    @Override
    public void close() throws Exception {
        encoder().endResource( resource() );
        parent().encodeNext();
    }
}
