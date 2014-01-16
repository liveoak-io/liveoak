/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.filesystem;

import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractHTTPResourceTestCase;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class HTTPFilesystemResourceTest extends AbstractHTTPResourceTestCase {

    @Override
    public RootResource createRootResource() {
        return new FilesystemResource("files");
    }

    @Override
    public ResourceState createConfig() {
        File dataDir = new File(this.projectRoot, "/target/test-data/one");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // create a file in there
        try {
            FileWriter out = new FileWriter(new File(dataDir, "file.txt"));
            out.write("0123456789");
            out.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a test file: ", e);
        }

        ResourceState config = new DefaultResourceState();
        config.putProperty("root", dataDir.getAbsolutePath());
        return config;
    }


    @Test
    public void testEnumerateRoot() throws Exception {

        HttpGet get = new HttpGet("http://localhost:8080/files");
        get.addHeader("Accept", "application/json");

        try {
            System.err.println("DO GET");
            CloseableHttpResponse result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            HttpEntity entity = result.getEntity();
            if (entity.getContentLength() > 0) {
                entity.writeTo(System.err);
            }
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);

        } finally {
            httpClient.close();
        }

    }

    @Test
    public void testReadChild() throws Exception {
        HttpGet get = new HttpGet("http://localhost:8080/files/file.txt");
        get.addHeader("Accept", "text/*");

        try {
            System.err.println("DO GET");
            CloseableHttpResponse result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            HttpEntity entity = result.getEntity();
            if (entity.getContentLength() > 0) {
                entity.writeTo(System.err);
            }
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);

        } finally {
            httpClient.close();
        }
    }

}
