package io.liveoak.filesystem.aggregating;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.common.DefaultResourceParams;
import io.liveoak.filesystem.aggregating.extension.AggregatingFilesystemExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractTestCase;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AggregatingFilesystemResourceTest extends AbstractTestCase {

    static {
        setProjectRoot(AggregatingFilesystemResourceTest.class);
        applicationDirectory = projectRoot;
        try {
            installTestApp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BeforeClass
    public static void setup() throws Exception {
        loadExtension("aggr-fs", new AggregatingFilesystemExtension());
        installTestAppResource("aggr-fs", "aggr", JsonNodeFactory.instance.objectNode());
    }

    @Test
    public void testReadConfig() throws Exception {
        Map<String, List<String>> params = new HashMap<>();
        params.put("runtime", new ArrayList<>());
        ResourceParams resourceParams = DefaultResourceParams.instance(params);
        ResourceState result = client.read(new RequestContext.Builder().resourceParams(resourceParams).build(), "/admin/applications/testApp/resources/aggr");
        assertThat(result.getProperty("directory")).isEqualTo(new File(applicationDirectory, "aggr").getAbsolutePath());
    }

}
