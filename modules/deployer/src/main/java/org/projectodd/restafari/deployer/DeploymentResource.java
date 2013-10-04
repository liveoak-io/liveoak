package org.projectodd.restafari.deployer;

import org.projectodd.restafari.spi.Resource;

/**
 * @author lball@redhat.com
 */
public class DeploymentResource implements Resource {

    private String id;

    public DeploymentResource(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
