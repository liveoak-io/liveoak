package org.projectodd.restafari.deployer;

import org.projectodd.restafari.spi.*;
import org.projectodd.restafari.spi.state.ResourceState;
import org.vertx.java.core.Vertx;

import java.io.IOException;

/**
 * Manages mBaaS
 *
 * @author lball@redhat.com
 */
public class DeployerResource implements RootResource, CollectionResource {

    private DeploymentPath deploymentPath;
    private Vertx vertx;

    @Override
    public void initialize(ResourceContext context) throws InitializationException {
        // Read controller context config and monitor a deployments directory
        try {
            vertx = context.vertx();
            String dir = context.config().get("mbaas.deployer.dir", System.getProperty("user.dir"));
            deploymentPath = new DeploymentPath(dir, vertx);
        } catch (IOException e) {
            System.err.println("Cannot initialize the deployer. " + e.getMessage());
        }
    }

    /**
     * Undeploys a
     */
    @Override
    public void destroy() {
    }

    @Override
    public void read(Pagination pagination, Responder responder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void create(ResourceState state, Responder responder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void writeMembers(ResourceSink sink) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Resource parent() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String id() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void read(String id, Responder responder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void delete(Responder responder) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
