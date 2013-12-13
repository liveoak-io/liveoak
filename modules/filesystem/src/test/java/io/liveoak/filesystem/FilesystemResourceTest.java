/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.Test;

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
    public ResourceState createConfig() {
        ResourceState config = new DefaultResourceState();
        config.putProperty("root", this.projectRoot.getAbsolutePath());
        return config;
    }

    @Test
    public void testRoot() throws Exception {
        ResourceState result = client.read(new RequestContext.Builder().build(), "/files");

        assertThat(result).isNotNull();
    }

    @Test
    public void testChild() throws Exception {
        ResourceState result = client.read(new RequestContext.Builder().build(), "/files/pom.xml");
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
