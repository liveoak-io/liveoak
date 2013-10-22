package org.projectodd.restafari.filesystem;

import org.junit.Ignore;
import org.junit.Test;
import org.projectodd.restafari.container.SimpleConfig;
import org.projectodd.restafari.spi.Config;
import org.projectodd.restafari.spi.resource.Resource;
import org.projectodd.restafari.spi.resource.RootResource;
import org.projectodd.restafari.spi.resource.async.BinaryResource;
import org.projectodd.restafari.testtools.AbstractResourceTestCase;

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
        Resource result = connector.read("/files");

        assertThat(result).isNotNull();
    }

    @Ignore
    @Test
    public void testChild() throws Exception {
        Resource result = connector.read("/files/pom.xml");

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(BinaryResource.class);
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
