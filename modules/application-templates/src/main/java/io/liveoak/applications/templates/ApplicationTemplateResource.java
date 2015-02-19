package io.liveoak.applications.templates;

import io.liveoak.common.util.ConversionUtils;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.MapResource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

/**
 * Created by mwringe on 10/02/15.
 */
public class ApplicationTemplateResource implements RootResource, SynchronousResource {

    String id;
    Resource parent;
    ObjectNode config;

    public ApplicationTemplateResource(String id) {
        this.id = id;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
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
    public void properties(RequestContext ctx, ResourceState configState) throws Exception {
        config = ConversionUtils.convert(configState);
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        ResourceState resourceState = ConversionUtils.convert(config);

        for (String propertyName : resourceState.getPropertyNames()) {
            Object property = resourceState.getProperty(propertyName);
            if (property instanceof ResourceState) {
                Object newProperty = toMapResource((ResourceState) property);
                sink.accept(propertyName, newProperty);
            } else {
                sink.accept(propertyName, property);
            }
        }
        sink.complete();
    }



    public ObjectNode getConfig() {
        return (ObjectNode)config.get("template");
    }

    private MapResource toMapResource(ResourceState state) {
        MapResource mapResource = new MapResource();

        for (String propertyName: state.getPropertyNames()) {
            Object property = state.getProperty(propertyName);
            if (property instanceof ResourceState) {
                mapResource.put(propertyName, toMapResource((ResourceState) property));
            } else if (property instanceof Collection) {
                mapResource.put(propertyName, handleCollection((Collection) property));
            } else {
                mapResource.put(propertyName, property);
            }
        }

        return mapResource;
    }

    private List handleCollection(Collection collection) {
        List<Object> list = new ArrayList<>();
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof ResourceState) {
                list.add(toMapResource((ResourceState) next));
            } else if (next instanceof Collection) {
                list.add(handleCollection((Collection)next));
            } else {
                list.add(next);
            }
        }
        return list;
    }
}
