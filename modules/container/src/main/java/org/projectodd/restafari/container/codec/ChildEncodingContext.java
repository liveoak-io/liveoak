package org.projectodd.restafari.container.codec;

import org.projectodd.restafari.spi.RequestContext;

/**
 * @author Bob McWhirter
 */
public class ChildEncodingContext extends AbstractEncodingContext implements EncodingContext {

    public ChildEncodingContext(RequestContext ctx, AbstractEncodingContext parent, Object object) {
        super( parent, ctx, object, ()->{
            parent.encodeNextContent();
        });
    }
}
