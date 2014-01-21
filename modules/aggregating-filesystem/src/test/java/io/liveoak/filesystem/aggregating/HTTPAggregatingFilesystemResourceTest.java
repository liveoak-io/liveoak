/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem.aggregating;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractHTTPResourceTestCase;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class HTTPAggregatingFilesystemResourceTest extends AbstractHTTPResourceTestCase {

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

    @Before
    public void before() {
        File dataDir = this.projectRoot;

        // create some files in there
        try {
            FileWriter out = new FileWriter(new File(dataDir, "aggregate.js.aggr"));
            out.write("require first.js\n");
            out.write("require second.js\n");
            out.close();

            out = new FileWriter(new File(dataDir, "first.js"));
            out.write("// first.js\n");
            out.close();

            out = new FileWriter(new File(dataDir, "second.js"));
            out.write("// second.js\n");
            out.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a test file: ", e);
        }
    }

    @After
    public void after() {
        new File(this.projectRoot, "aggregate.js.aggr").delete();
        new File(this.projectRoot, "first.js").delete();
        new File(this.projectRoot, "second.js").delete();
    }

    @Test
    public void testReadAggregate() throws Exception {

        HttpGet get = new HttpGet("http://localhost:8080/aggr/aggregate.js");
        get.addHeader("Accept", "*/*");

        try {
            System.err.println("DO GET");
            CloseableHttpResponse result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            HttpEntity entity = result.getEntity();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (entity.getContentLength() > 0) {
                entity.writeTo(out);
            }
            String content = new String(out.toByteArray());
            System.err.println(content);
            System.err.println("\n<<<=============");

            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(content).isEqualTo("// first.js\n// second.js\n");

        } finally {
            httpClient.close();
        }
    }
}
