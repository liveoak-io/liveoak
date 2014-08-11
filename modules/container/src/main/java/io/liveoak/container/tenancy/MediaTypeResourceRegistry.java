package io.liveoak.container.tenancy;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import io.liveoak.container.protocols.http.HttpResourceRequestDecoder;
import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.jboss.logging.Logger;

/**
 * @author Ken Finnigan
 */
public class MediaTypeResourceRegistry implements MountPointResource, RootResource, SynchronousResource {
    public MediaTypeResourceRegistry(Resource parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    public MediaTypeResourceRegistry(String id) {
        this.id = id;
    }

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

    @Override
    public void registerResource(Resource resource) {
        registerResource(resource, MediaType.JSON, true);
    }

    @Override
    public void registerResource(Resource resource, MediaType mediaType, boolean makeDefault) {
        Map<MediaType, Resource> resourceMap = this.registry.get(resource.id());

        if (resourceMap == null) {
            resourceMap = new ConcurrentHashMap<>();
            this.registry.put(resource.id(), resourceMap);
        }

        resourceMap.put(mediaType, resource);

        if (makeDefault || !resourceMap.containsKey(DEFAULT)) {
            resourceMap.put(DEFAULT, resource);
        }
    }

    @Override
    public void unregisterResource(Resource resource) {
        unregisterResource(resource, MediaType.JSON);
    }

    @Override
    public void unregisterResource(Resource resource, MediaType mediaType) {
        Map<MediaType, Resource> resourceMap = this.registry.get(resource.id());
        resourceMap.remove(mediaType);

        if (resourceMap.size() == 1 && resourceMap.get(DEFAULT) != null) {
            resourceMap.remove(DEFAULT);
        }

        if (resourceMap.isEmpty()) {
            this.registry.remove(resource.id());
        }
    }

    @Override
    public void readMember(RequestContext ctx, String id, Responder responder) throws Exception {
        Map<MediaType, Resource> resourceMap = this.registry.get(id);

        if (resourceMap == null) {
            responder.noSuchResource(id);
            return;
        }

        MediaType mediaType = mediaType(ctx);

        Resource member = resourceMap.get(mediaType);
        if (member == null) {
            member = resourceMap.get(DEFAULT);
        }

        if (member == null) {
            responder.noSuchResource(id);
            return;
        }

        responder.resourceRead(member);
    }

    @Override
    public void readMembers(RequestContext ctx, ResourceSink sink) throws Exception {
        if (!this.registry.isEmpty()) {
            MediaType mediaType = mediaType(ctx);

            for (Map<MediaType, Resource> resourceMap : this.registry.values()) {
                Resource resource = resourceMap.get(mediaType);
                if (resource == null) {
                    resource = resourceMap.get(DEFAULT);
                }

                sink.accept(resource);
            }
        }

        sink.close();
    }

    @Override
    public Collection<Resource> members() {
        return this.registry.values().stream().flatMap(resourceList -> resourceList.values().stream().distinct()).collect(Collectors.toList());
    }

    @Override
    public Resource member(String id) {
        Map<MediaType, Resource> resourceMap = this.registry.get(id);
        if (resourceMap != null) {
            return resourceMap.get(DEFAULT);
        }
        return null;
    }

    private MediaType mediaType(RequestContext ctx) {
        try {
            DefaultHttpRequest request = (DefaultHttpRequest) ctx.requestAttributes().getAttribute(HttpResourceRequestDecoder.HTTP_REQUEST);
            String contentTypeHeader = request.headers().get(HttpHeaders.Names.CONTENT_TYPE);
            return new MediaType(contentTypeHeader);
        } catch (NullPointerException e) {
            log.trace("Unable to retrieve Content-Type from Request. Defaulting to 'application/json'.");
        }

        return MediaType.JSON;
    }

    private static MediaType DEFAULT = new MediaType("DEFAULT/none");
    private Resource parent;
    private String id;
    private Map<String, Map<MediaType, Resource>> registry = new ConcurrentHashMap<>();

    static final Logger log = Logger.getLogger(MediaTypeResourceRegistry.class);
}
