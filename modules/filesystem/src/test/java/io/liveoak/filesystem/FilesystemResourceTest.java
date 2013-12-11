/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem;

import java.util.concurrent.CompletableFuture;

import io.liveoak.container.ResourceErrorResponse;
import io.liveoak.container.codec.DefaultResourceState;
import io.liveoak.container.codec.binary.DefaultBinaryResourceState;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class FilesystemResourceTest extends AbstractResourceTestCase {

    @Override
    public RootResource createRootResource() {
        return new FilesystemResource("files", true);
    }

    @Override
    public ResourceState createConfig() {
        ResourceState config = new DefaultResourceState();
        config.putProperty("root", this.projectRoot.getAbsolutePath());
        return config;
    }

    @Test
    public void testRoot() throws Exception {
        ResourceState result = connector.read(new RequestContext.Builder().build(), "/files");

        assertThat(result).isNotNull();
    }

    @Test
    public void testChild() throws Exception {
        ResourceState result = connector.read(new RequestContext.Builder().build(), "/files/pom.xml");
        assertThat(result).isNotNull();
    }

    @Test
    public void testFile() throws Exception {
        DefaultResourceState state = new DefaultResourceState("file-test.txt");
        state.putProperty("type", "file");

        // create
        ResourceState result = connector.create(new RequestContext.Builder().build(), "/files/target", state);
        assertThat(result).isNotNull();

        // write
        byte[] bytes = "File System Test - Can delete this file...\n".getBytes();
        ByteBuf buf = Unpooled.buffer(bytes.length);
        buf.writeBytes(bytes);
        result = connector.update(new RequestContext.Builder().build(), "files/target/file-test.txt", new DefaultBinaryResourceState(buf));
        assertThat(result).isNotNull();

        // delete
        result = connector.delete(new RequestContext.Builder().build(), "files/target/file-test.txt");
        assertThat(result).isNotNull();
    }

    @Test
    public void testCreateFileExists() throws Exception {
        DefaultResourceState state = new DefaultResourceState("target");

        CompletableFuture<ResourceErrorResponse> future = new CompletableFuture<>();
        connector.create(new RequestContext.Builder().build(), "/files", state, response -> {
            if (response instanceof ResourceErrorResponse) {
                future.complete((ResourceErrorResponse) response);
            } else {
                future.completeExceptionally(new Exception("Expected an error response but got " + response));
            }
        });

        ResourceErrorResponse response = future.get();
        assertThat(response).isNotNull();
        assertThat(response.errorType()).isEqualTo(ResourceErrorResponse.ErrorType.RESOURCE_ALREADY_EXISTS);
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
