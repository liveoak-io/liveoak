/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.io.ByteArrayOutputStream;

import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.junit.Ignore;
import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class HttpGridFSHugeBlobTest extends AbstractGridFSTest {

    @Test
    public void testReadWriteHugeBlob() throws Exception {
        HttpPut put = new HttpPut("http://localhost:8080/testOrg/testApp/gridfs/john/vacation/mars_2038/beach.jpg");

        put.setHeader(HttpHeaders.Names.CONTENT_TYPE, "image/jpeg");
        put.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        int size = 10 * 1024 * 1024;
        InputStreamEntity entity = new InputStreamEntity(new SampleInputStream(size), size, ContentType.create("text/plain", "UTF-8"));

        put.setEntity(entity);

        JsonObject json = null;
        try {
            System.err.println("DO PUT - " + put.getURI());

            CloseableHttpResponse result = httpClient.execute(put);

            System.err.println("=============>>>");
            System.err.println(result);

            HttpEntity resultEntity = result.getEntity();

            assertThat(resultEntity.getContentLength()).isGreaterThan(0);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resultEntity.writeTo(baos);

            String resultStr = new String(baos.toByteArray());
            System.err.println(resultStr);
            json = new JsonObject(resultStr);

            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(201);
            assertThat(resultEntity.getContentType().getValue()).isEqualTo(APPLICATION_JSON);

            // do some more assertions on the response
            assertThat(json.getObject("self").getString("href")).startsWith("/testOrg/testApp/gridfs/john/.files/");
            assertThat(json.getString("filename")).isEqualTo("beach.jpg");
            String blobId = json.getString("id");
            assertThat(blobId).isNotEqualTo("beach.jpg");
            assertThat(json.getNumber("length")).isEqualTo(size);

            JsonArray links = json.getArray("links");
            assertThat(links).isNotNull();
            assertThat(links.size()).isEqualTo(3);
            assertLink(links.get(0), "self", "/testOrg/testApp/gridfs/john/vacation/mars_2038/beach.jpg;meta");
            assertLink(links.get(1), "parent", "/testOrg/testApp/gridfs/john/vacation/mars_2038");
            assertLink(links.get(2), "blob", "/testOrg/testApp/gridfs/john/vacation/mars_2038/beach.jpg");



            // now read the blob
            HttpGet get = new HttpGet("http://localhost:8080/testOrg/testApp/gridfs/john/vacation/mars_2038/beach.jpg");
            get.setHeader(HttpHeaders.Names.ACCEPT, ALL);

            System.err.println("DO GET - " + get.getURI());
            result = httpClient.execute(get);
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);

            System.err.println("=============>>>");
            System.err.println(result);

            resultEntity = result.getEntity();
            assertThat(resultEntity.getContentLength()).isGreaterThan(0);
            CountOutputStream counter = new CountOutputStream();
            resultEntity.writeTo(counter);
            System.err.println("\n... content not displayed ...\n<<<=============");
            assertThat(counter.getCount()).isEqualTo(size);
            // TODO: make this pass
            //assertThat(resultEntity.getContentType().getValue()).isEqualTo("image/jpeg");

        } finally {
            httpClient.close();
        }
    }
}
