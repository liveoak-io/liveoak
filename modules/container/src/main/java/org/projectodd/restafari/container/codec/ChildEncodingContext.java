package org.projectodd.restafari.container.codec;

/**
 * @author Bob McWhirter
 */
public class ChildEncodingContext extends AbstractEncodingContext implements EncodingContext {

    public ChildEncodingContext(AbstractEncodingContext parent, Object object) {
        super( parent, object, ()->{
            parent.encodeNextContent();
        });
    }
}
