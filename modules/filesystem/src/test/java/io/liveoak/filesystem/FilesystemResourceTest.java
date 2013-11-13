package io.liveoak.filesystem;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Ignore;
import org.junit.Test;
import io.liveoak.container.SimpleConfig;
import io.liveoak.spi.Config;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.BinaryResource;
import io.liveoak.testtools.AbstractResourceTestCase;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class FilesystemResourceTest extends AbstractResourceTestCase {

    @Override
    public RootResource createRootResource() {
        return new FilesystemResource("files");
    }

    @Override
    public Config createConfig() {
        SimpleConfig config = new SimpleConfig();
        config.put("root", "./tmp");
        return config;
    }

    @Ignore
    @Test
    public void testRoot() throws Exception {
        ResourceState result = connector.read( new RequestContext.Builder().build(), "/files");

        assertThat(result).isNotNull();
    }

    @Ignore
    @Test
    public void testChild() throws Exception {
        ResourceState result = connector.read( new RequestContext.Builder().build(), "/files/pom.xml");
        assertThat(result).isNotNull();
    }

    /*
    @Test
    public void testEncoding() throws Exception {
        ByteBuf output = Unpooled.buffer();
        JsonEncoder encoder = new JsonEncoder();

        JsonGenerator generator = encoder.createEncodingAttachment(output);

        EncodingContext<JsonGenerator> context = new EncodingContext<JsonGenerator>(encoder, generator, this.resource);

        System.err.println("ROOT CONTEXT: " + context);

        CountDownLatch latch = new CountDownLatch(1);

        context.encode(() -> {
            latch.countDown();
            return null;
        });

        latch.await();

        System.err.println(output.toString(Charset.defaultCharset()));
    }
    */

}
