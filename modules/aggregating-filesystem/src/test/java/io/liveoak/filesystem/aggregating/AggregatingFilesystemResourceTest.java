package io.liveoak.filesystem.aggregating;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AggregatingFilesystemResourceTest extends AbstractResourceTestCase {

    @Override
    public RootResource createRootResource() {
        return new AggregatingFilesystemResource("aggr");
    }

    @Override
    public ResourceState createConfig() {
        ResourceState config = new DefaultResourceState();
        config.putProperty("root", this.projectRoot.getAbsolutePath());
        return config;
    }

    @Test
    public void testReadConfig() throws Exception {
        ResourceState result = client.read(new RequestContext.Builder().build(), "/aggr;config");
        assertThat(result.getProperty("root")).isEqualTo(this.projectRoot.getAbsolutePath());
    }

}
