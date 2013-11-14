package io.liveoak.container.codec.driver;

import io.liveoak.container.codec.Encoder;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ReturnFields;
import io.liveoak.spi.resource.async.Resource;

import java.util.LinkedList;

/**
 * @author Bob McWhirter
 */
public abstract class AbstractEncodingDriver implements EncodingDriver {

    public AbstractEncodingDriver(Object object, ReturnFields returnFields) {
        this(null, object, returnFields);
    }

    public AbstractEncodingDriver(EncodingDriver parent, Object object, ReturnFields returnFields) {
        this.parent = parent;
        this.object = object;
        this.returnFields = returnFields;
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

    protected ReturnFields returnFields() {
        return this.returnFields;
    }

    @Override
    public Object object() {
        return this.object;
    }

    @Override
    public EncodingDriver parent() {
        return this.parent;
    }

    public int depth() {
        int depth = 0;
        EncodingDriver current = this;
        while ( current != null ) {
            ++depth;
            current = current.parent();
        }
        return depth;
    }

    public void encodeNext() {
        if (children.isEmpty()) {
            try {
                close();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else {
            EncodingDriver next = children.removeFirst();
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
    private ReturnFields returnFields;

    private LinkedList<EncodingDriver> children = new LinkedList<>();

}
