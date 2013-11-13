package io.liveoak.container.codec.driver;

import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;

/**
 * @author Bob McWhirter
 */
public class MembersEncodingDriver extends ResourceEncodingDriver {

    public MembersEncodingDriver(EncodingDriver parent, Resource resource) {
        super(parent, resource);
    }

    @Override
    public void encode() throws Exception {
        System.err.println( "members::encode" );
        resource().readMembers(requestContext(), new MyResourceSink());
    }

    @Override
    public void close() throws Exception {
        //encoder().endMembers();
        if ( hasMembers ) {
            encoder().endMembers();
        }
        System.err.println( "members::close" );
        parent().encodeNext();
    }

    private class MyResourceSink implements ResourceSink {

        @Override
        public void accept(Resource resource) {
            if ( ! hasMembers ) {
                try {
                    encoder().startMembers();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                hasMembers = true;
            }
            addChildDriver( new ResourceEncodingDriver( MembersEncodingDriver.this, resource ) );
        }

        @Override
        public void close() {
            System.err.println( "member sink close" );
            encodeNext();
        }
    }

    private boolean hasMembers;
}
