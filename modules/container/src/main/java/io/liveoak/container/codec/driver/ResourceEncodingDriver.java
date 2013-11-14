package io.liveoak.container.codec.driver;

import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author Bob McWhirter
 */
public class ResourceEncodingDriver extends AbstractEncodingDriver {

    public ResourceEncodingDriver(Resource resource, ReturnFields returnFields) {
        super(resource, returnFields);
    }

    public ResourceEncodingDriver(EncodingDriver parent, Resource resource, ReturnFields returnFields) {
        super(parent, resource, returnFields);
    }

    public Resource resource() {
        return (Resource) object();
    }

    @Override
    public void encode() throws Exception {
        encoder().startResource(resource());
        addChildDriver(new PropertiesEncodingDriver(this, resource(), returnFields()));
        addChildDriver(new MembersEncodingDriver(this, resource(), returnFields()));
        encodeNext();
    }

    @Override
    public void close() throws Exception {
        encoder().endResource(resource());
        parent().encodeNext();
    }
}
