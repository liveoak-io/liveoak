package io.liveoak.container.tenancy;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.liveoak.spi.MediaType;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.Resource;
import io.netty.handler.codec.http.HttpHeaders;
import org.jboss.logging.Logger;

import static io.liveoak.spi.RequestType.*;

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
    public synchronized void registerResource(Resource resource, MediaType mediaType, boolean makeDefault) {
        Map<MediaType, Resource> resourceMap = this.registry.get(resource.id());

        if (resourceMap == null) {
            resourceMap = new ConcurrentHashMap<>();
            this.registry.put(resource.id(), resourceMap);
        }

        resourceMap.put(mediaType, resource);

        if (makeDefault) {
            resourceMap.put(DEFAULT, resource);
        }
    }

    @Override
    public void unregisterResource(Resource resource) {
        unregisterResource(resource, MediaType.JSON);
    }

    @Override
    public synchronized void unregisterResource(Resource resource, MediaType mediaType) {
        if (this.registry != null) {
            Map<MediaType, Resource> resourceMap = this.registry.get(resource.id());

            if (resourceMap != null) {
                resourceMap.remove(mediaType);

                if (resourceMap.size() == 1 && resourceMap.get(DEFAULT) != null) {
                    resourceMap.remove(DEFAULT);
                }

                if (resourceMap.isEmpty()) {
                    this.registry.remove(resource.id());
                }
            }
        }
    }

    @Override
    public Resource member(RequestContext ctx, String id) throws Exception {
        Map<MediaType, Resource> resourceMap = this.registry.get(id);

        if (resourceMap == null) {
            return null;
        }

        MediaType mediaType = mediaType(ctx);

        Resource member = resourceMap.get(mediaType);
        if (member == null) {
            member = resourceMap.get(DEFAULT);
        }

        if (member == null) {
            log.trace("We can't handle this mediatype: " + mediaType + " (resourceMap: " + resourceMap + ")");
            return null;
        }

        return member;
    }

    @Override
    public Collection<Resource> members(RequestContext ctx) throws Exception {
        LinkedList<Resource> members = new LinkedList<>();

        if (!this.registry.isEmpty()) {
            MediaType mediaType = mediaType(ctx);

            for (Map<MediaType, Resource> resourceMap : this.registry.values()) {
                Resource resource = resourceMap.get(mediaType);
                if (resource == null) {
                    resource = resourceMap.get(DEFAULT);
                }

                members.add(resource);
            }
        }
        return members;
    }

    private MediaType mediaType(RequestContext ctx) {
        if (ctx.requestType() != null) {
            switch (ctx.requestType()) {
                case CREATE:
                case UPDATE:
                    return contentType(ctx);
                case READ:
                case DELETE:
                    return accept(ctx);
            }
        }

        return MediaType.JSON;
    }

    private MediaType contentType(RequestContext ctx) {
        try {
            MediaType mediaType = (MediaType) ctx.requestAttributes().getAttribute(HttpHeaders.Names.CONTENT_TYPE);
            if (mediaType != null) {
                return mediaType;
            }
        } catch (NullPointerException e) {
            log.trace("Unable to retrieve Content-Type header from Request. Defaulting to 'application/json'.");
        }
        return MediaType.JSON;
    }

    private MediaType accept(RequestContext ctx) {
        try {
            MediaType mediaType = (MediaType) ctx.requestAttributes().getAttribute(HttpHeaders.Names.ACCEPT);
            if (mediaType != null) {
                return mediaType;
            }
        } catch (NullPointerException e) {
            log.trace("Unable to retrieve Accept header from Request. Defaulting to 'application/json'.");
        }
        return MediaType.JSON;
    }

    public String toString() {
        return "[" + getClass().getSimpleName() + ": parent=" + this.parent + "; id=" + this.id + "; registry=" + this.registry.values() + "]";
    }

    private static MediaType DEFAULT = new MediaType("DEFAULT/none");
    private Resource parent;
    private String id;
    private Map<String, Map<MediaType, Resource>> registry = new ConcurrentHashMap<>();

    static final Logger log = Logger.getLogger(MediaTypeResourceRegistry.class);
}
