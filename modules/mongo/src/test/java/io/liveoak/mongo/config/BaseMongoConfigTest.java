package io.liveoak.mongo.config;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.container.ReturnFieldsImpl;
import io.liveoak.mongo.RootMongoResource;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import org.fest.assertions.Fail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.Vertx;

import javax.management.remote.rmi._RMIConnectionImpl_Tie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseMongoConfigTest {

    LiveOakSystem system;
    Client client;
    RootResource resource;

    static String BASEPATH = "storage";

    //@Before
    public void setUpSystem(ResourceState config) throws Exception {
        this.system = LiveOakFactory.create();
        this.resource = new RootMongoResource("storage");
        this.system.directDeployer().deploy(this.resource, config);
        this.client = this.system.client();
    }

    @After
    public void tearDownSystem() throws Exception {
        if (system != null) {
            this.system.stop();
        }
    }
}
