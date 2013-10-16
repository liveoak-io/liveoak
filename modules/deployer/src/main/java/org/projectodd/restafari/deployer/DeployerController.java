package org.projectodd.restafari.deployer;

import org.projectodd.restafari.spi.*;
import org.vertx.java.core.Vertx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Manages mBaaS {@linkplain DeploymentResource}s
 *
 * @author lball@redhat.com
 */
public class DeployerController implements ResourceController {

    private DeploymentPath deploymentPath;
    private Vertx vertx;

    @Override
    public void initialize(ControllerContext context) throws InitializationException {
        // Read controller context config and monitor a deployments directory
        try {
            vertx = context.getVertx();
            String dir = context.getConfig().get("mbaas.deployer.dir", System.getProperty("user.dir"));
            deploymentPath = new DeploymentPath(dir, vertx);
        } catch (IOException e) {
            System.err.println("Cannot initialize the deployer. " + e.getMessage());
        }
    }

    /**
     * Undeploys a {@linkplain DeploymentResource}
     */
    @Override
    public void destroy() {
    }

    /**
     * Gets a {@linkplain DeploymentResource} for the id provided, if one exists.
     * @param context The request context.
     * @param collectionName The name of the collection - currently not used.
     *                       If there are more than 1 deployment paths that we watch for changes,
     *                       this could specify a named deployment path.
     * @param id The name of the {@linkplain DeploymentResource}
     * @param responder The responder to provide the response.
     */
    @Override
    public void getResource(RequestContext context, String collectionName, String id, Responder responder) {
        responder.resource(new DeploymentResource(id));
    }

    /**
     * Gets all of the {@linkplain DeploymentResource}s currently known
     * @param context The request context.
     * @param collectionName The name of the collection - currently not used.
     *                       If there are more than 1 deployment paths that we watch for changes,
     *                       this could specify a named deployment path.
     * @param pagination The pagination details.
     * @param responder The responder to provide the response.
     */
    @Override
    public void getResources(RequestContext context, String collectionName, Pagination pagination, Responder responder) {
        // Return the current deployments as a set of resources
        Collection<Resource> resources = new ArrayList<>();
        responder.resources(resources);
    }

    /**
     * Creates a new {@linkplain DeploymentResource} and notifies the mBaaS registry of the deployment.
     * @param context The request context.
     * @param collectionName The name of the collection - currently not used.
     *                       If there are more than 1 deployment paths that we watch for changes,
     *                       this could specify a named deployment path.
     * @param resource The new resource state.
     * @param responder The responder to provide the response.
     */
    @Override
    public void createResource(RequestContext context, String collectionName, Resource resource, Responder responder) {
        // Deploy a new controller?
    }

    /**
     * Updates a deployment and notifies the registry of changes
     * @param context The request context.
     * @param collectionName The name of the collection - currently not used.
     *                       If there are more than 1 deployment paths that we watch for changes,
     *                       this could specify a named deployment path.
     * @param id The identifier of the resource.
     * @param resource The new resource state.
     * @param responder The responder to provide the response.
     */
    @Override
    public void updateResource(RequestContext context, String collectionName, String id, Resource resource, Responder responder) {
        // Update a controller's configuration?
    }

    /**
     * Deletes, and therefore undeploys a {@linkplain DeploymentResource}
     * @param context The request context.
     * @param collectionName The name of the collection - currently not used.
     *                       If there are more than 1 deployment paths that we watch for changes,
     *                       this could specify a named deployment path.
     * @param id The identifier of the resource.
     * @param responder The responder to provide the response.
     */
    @Override
    public void deleteResource(RequestContext context, String collectionName, String id, Responder responder) {
        // again - just undeploy the controller?
    }

    @Override
    public void getCollections(RequestContext context, Pagination pagination, Responder responder) {
        responder.internalError("TODO: not yet implemented");
    }

    @Override
    public void deleteCollection(RequestContext requestContext, String collectionName, Responder responder) {
        responder.internalError("TODO: not yet implemented");
    }
}
