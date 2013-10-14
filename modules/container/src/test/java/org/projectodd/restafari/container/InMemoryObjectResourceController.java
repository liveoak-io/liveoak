package org.projectodd.restafari.container;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.projectodd.restafari.spi.ControllerContext;
import org.projectodd.restafari.spi.ObjectResource;
import org.projectodd.restafari.spi.Pagination;
import org.projectodd.restafari.spi.RequestContext;
import org.projectodd.restafari.spi.Resource;
import org.projectodd.restafari.spi.ResourceController;
import org.projectodd.restafari.spi.Responder;

public class InMemoryObjectResourceController implements ResourceController {

    public void addCollection(String name) {
        this.resources.put(name, new HashMap<String, Resource>());
    }

    @Override
    public void initialize(ControllerContext context) {
        this.context = context;
    }

    @Override
    public void destroy() {
        this.context = null;
    }

    @Override
    public void getResource(RequestContext context, String collectionName, String id, Responder responder) {
        System.err.println("Retrieving resource for id " +  id);
        Map<String, Resource> collection = this.resources.get(collectionName);
        if (collection == null) {
            responder.noSuchCollection(collectionName);
            return;
        }

        Resource resource = collection.get(id);
        if (resource == null) {
            responder.noSuchResource(id);
            return;
        }

        responder.resource(resource);
    }

    @Override
    public void getResources(RequestContext context, String collectionName, Pagination pagination, Responder responder) {
        System.err.println("Retrieving resources for collection " +  collectionName);
        Map<String, Resource> collection = this.resources.get(collectionName);
        if (collection == null) {
            System.err.println("no such collection: " + collectionName);
            responder.noSuchCollection(collectionName);
            return;
        }

        responder.resources(collection.values());
    }

    @Override
    public void createResource(RequestContext context, String collectionName, Resource resource, Responder responder) {
        System.err.println("Creating resource for collection " +  collectionName);
        Map<String, Resource> collection = this.resources.get(collectionName);
        if (collection == null) {
            responder.noSuchCollection(collectionName);
            return;
        }

        if (resource == null) {
            resource = new SimpleObjectResource();
        }

        String id = UUID.randomUUID().toString();
        resource.setId(id);
        collection.put(id, resource);

        responder.resourceCreated(resource);
    }

    @Override
    public void updateResource(RequestContext context, String collectionName, String id, Resource resource, Responder responder) {
        System.err.println("Updating resource for id " +  id);
        Map<String, Resource> collection = this.resources.get(collectionName);
        if (collection == null) {
            responder.noSuchCollection(collectionName);
            return;
        }

        boolean created = false;

        if (!collection.containsKey(id)) {
            created = true;
        }

        collection.put(id, resource);

        if (created) {
            resource.setId(id);
            responder.resourceCreated(resource);
        } else {
            responder.resourceUpdated(resource);
        }
    }

    @Override
    public void deleteResource(RequestContext context, String collectionName, String id, Responder responder) {
        System.err.println("Deleting resource for id " +  id);
        Map<String, Resource> collection = this.resources.get(collectionName);
        if (collection == null) {
            responder.noSuchCollection(collectionName);
            return;
        }

        Resource removed = collection.remove(id);

        if (removed == null) {
            responder.noSuchResource(id);
        } else {
            responder.resourceDeleted(removed);
        }

    }

    private ControllerContext context;
    private Map<String, Map<String, Resource>> resources = new HashMap<>();

}
