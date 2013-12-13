package io.liveoak.spi.container;

import java.util.function.Consumer;

import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author Bob McWhirter
 */
public interface Deployer {

    public static class DeploymentResult {

        public DeploymentResult(RootResource rootResource) {
            this.rootResource = rootResource;
        }

        public DeploymentResult(Throwable cause) {
            this.cause = cause;
        }

        public RootResource rootResource() {
            return this.rootResource;
        }

        public Throwable cause() {
            return this.cause;
        }

        private RootResource rootResource;
        private Throwable cause;

    }
    void deploy(String id, ResourceState deploymentDescriptor, Consumer<DeploymentResult> callback) throws DeploymentException;
}
