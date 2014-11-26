package io.liveoak.redirect.https.resource;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class Redirect implements Resource {

    Resource parent;
    String id;

    public static final String REDIRECT = "redirects";
    public static final String TYPE = "redirect-type";
    public static final String MAX_AGE = "max-age";

    protected Options option;
    protected Types type;
    protected Integer maxAge;

    public enum Options  {
        ALL,NONE,SECURED
    }

    public enum Types {
        PERMANENT, TEMPORARY
    }

    public Redirect(String id) {
        this.id = id;
    }

    public Redirect(Options option, Types type, Integer maxAge) {
        this.option = option;
        this.type = type;
        this.id = null;
        this.maxAge = maxAge;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(REDIRECT, option);
        sink.accept(TYPE, type);
        sink.accept(MAX_AGE, maxAge);


        sink.complete();
    }

    public Options redirect() {
        return option;
    }

    public Types type() {
        return type;
    }

    public Integer maxAge() {
        return maxAge;
    }
}
