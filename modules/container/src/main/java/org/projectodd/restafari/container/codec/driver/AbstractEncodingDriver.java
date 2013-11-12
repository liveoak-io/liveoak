package org.projectodd.restafari.container.codec.driver;

import org.projectodd.restafari.container.codec.Encoder;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.resource.async.Resource;

import java.util.LinkedList;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractEncodingDriver implements EncodingDriver {

    public AbstractEncodingDriver(Resource resource) {
        this(null, resource);
    }

    public AbstractEncodingDriver(EncodingDriver parent, Object object) {
        this.parent = parent;
        this.object = object;
    }

    @Override
    public Encoder encoder() {
        if (this.parent != null) {
            return this.parent.encoder();
        }
        return null;
    }

    @Override
    public RequestContext requestContext() {
        if (this.parent != null) {
            return this.parent.requestContext();
        }
        return null;
    }

    @Override
    public Object object() {
        return this.object;
    }

    EncodingDriver parent() {
        return this.parent;
    }

    public void encodeNext() {
        if (children.isEmpty()) {
            try {
                System.err.println("encode next done, close: " + this);
                close();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else {
            EncodingDriver next = children.removeFirst();
            System.err.println( "next; " + next );
            try {
                next.encode();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    void addChildDriver(EncodingDriver child) {
        this.children.add(child);
    }

    private EncodingDriver parent;
    private Object object;

    private LinkedList<EncodingDriver> children = new LinkedList<>();

}
