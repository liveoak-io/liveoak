package io.liveoak.scripts.objects.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.liveoak.common.DefaultRequestContext;
import io.liveoak.common.DefaultResourceParams;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.ReturnFieldsImpl;
import io.liveoak.scripts.objects.LocalClient;
import io.liveoak.scripts.objects.Resource;
import io.liveoak.scripts.objects.Util;
import io.liveoak.scripts.objects.scripting.ScriptingPagination;
import io.liveoak.spi.Pagination;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceException;
import io.liveoak.spi.ResourceNotFoundException;
import io.liveoak.spi.Sorting;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class LiveOakClient implements LocalClient {

    Client client;

    public LiveOakClient(Client client) {
        this.client = client;
    }

    @Override
    public Resource create(String path, Resource resource) throws Exception {
        try {
            ResourceState resourceState = client.create(new DefaultRequestContext.Builder().build(), path, toResourceState(resource));
            return new LiveOakResource(resourceState);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Resource read(String path) throws Exception {
        try {
            ResourceState resourceState = client.read(new DefaultRequestContext.Builder().build(), path);
            return new LiveOakResource(resourceState);
        } catch (ResourceNotFoundException e) {
            //on a read it probably makes sense to just return null rather than throw an error.
            return null;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Resource update(String path, Resource resource) throws Exception {
        try {
            ResourceState resourceState = client.update(new DefaultRequestContext.Builder().build(), path, toResourceState(resource));
            return new LiveOakResource(resourceState);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Resource delete(String path) throws Exception {
        try {
            ResourceState resourceState = client.delete(new DefaultRequestContext.Builder().build(), path);
            return new LiveOakResource(resourceState);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Resource create(String path, Resource resource, Map<String, Object> parameters) throws Exception {
        try {
            ResourceState resourceState = client.create(generateRequestContext(parameters), path, toResourceState(resource));
            return new LiveOakResource(resourceState);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Resource read(String path, Map<String, Object> parameters) throws Exception {
        try {
            ResourceState resourceState = client.read(generateRequestContext(parameters), path);
            return new LiveOakResource(resourceState);
        } catch (ResourceNotFoundException e) {
            //on a read it probably makes sense to just return null rather than throw an error.
            return null;
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Resource update(String path, Resource resource, Map<String, Object> parameters) throws Exception {
        try {
            ResourceState resourceState = client.update(generateRequestContext(parameters), path, toResourceState(resource));
            return new LiveOakResource(resourceState);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Resource delete(String path, Map<String, Object> parameters) throws Exception {
        try {
            ResourceState resourceState = client.delete(generateRequestContext(parameters), path);
            return new LiveOakResource(resourceState);
        } catch (Exception e) {
            throw handleException(e);
        }
    }


    private ResourceState toResourceState(Resource resource) throws Exception {
        ResourceState resourceState = new DefaultResourceState(resource.getId());

        String uri = resource.getUri();
        if (uri != null) {
            resourceState.uri(new URI(resource.getUri()));
        }

        for (Map.Entry<String, Object> entry : resource.getProperties().entrySet()) {
            resourceState.putProperty(entry.getKey(), entry.getValue());
        }

        return resourceState;
    }

    private RequestContext generateRequestContext(Map<String,Object> parameters) {
        RequestContext.Builder builder = new RequestContext.Builder();

        Object offsetParameter = parameters.get("offset");
        int offset = Util.getIntValue(offsetParameter, Pagination.DEFAULT_OFFSET);
        parameters.remove("offset");
        Object limitParameter = parameters.get("limit");
        int limit = Util.getIntValue(limitParameter, Pagination.DEFAULT_LIMIT);
        parameters.remove("limit");
        builder.pagination(new ScriptingPagination(offset, limit));

        Object fields = parameters.get("fields");
        if (fields != null || fields instanceof String) {
            builder.returnFields(new ReturnFieldsImpl((String)fields));
            parameters.remove("fields");
        }

        Object sort = parameters.get("sort");
        if (sort != null || sort instanceof String) {
            builder.sorting(new Sorting((String)sort)) ;
            parameters.remove("sort");
        }

        Map<String, List<String>> parameterMap = new HashMap<>();
        for (String name: parameters.keySet()) {
            Object value = parameters.get(name);
            if (value != null) {
                parameterMap.put(name, Arrays.asList(value.toString()));
            }
        }

        builder.resourceParams(DefaultResourceParams.instance(parameterMap));

        return builder.build();
    }

    private Exception handleException (Exception e) {
        if (e instanceof ResourceException) {
            return Util.convertException((ResourceException)e);
        } else {
            return e;
        }
    }
}
