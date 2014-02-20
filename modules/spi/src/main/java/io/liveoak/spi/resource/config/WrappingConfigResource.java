package io.liveoak.spi.resource.config;

import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class WrappingConfigResource implements RootResource, ConfigResource {

    private final String id;
    private Resource parent;
    private final Resource configurable;

    public WrappingConfigResource(String id, Resource configurable) {
        this.id = id;
        this.configurable = configurable;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return this.id;
    }

    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        readConfigProperties(ctx, sink, this.configurable );
        sink.close();
    }

    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        updateConfigProperties(ctx, state, responder, this.configurable );
        responder.resourceUpdated(this);
    }

}
