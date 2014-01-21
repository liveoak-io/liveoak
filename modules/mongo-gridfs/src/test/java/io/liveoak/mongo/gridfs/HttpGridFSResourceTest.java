/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Ignore;
import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class HttpGridFSResourceTest extends AbstractGridFSTest {

    private static final String BLOB_CONTENT_TYPE = "application/octet-stream";

    @Test
    public void testCreateReadBlob() throws Exception {

        HttpPut put = new HttpPut("http://localhost:8080/testOrg/testApp/gridfs/john/vacation/italy_2013/beach.jpg");
        put.setHeader(HttpHeaders.Names.CONTENT_TYPE, BLOB_CONTENT_TYPE);
        put.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

        String content = "01234567890123456789";
        StringEntity entity = new StringEntity(content, ContentType.create("text/plain", "UTF-8"));
        put.setEntity(entity);

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
            JsonObject json = new JsonObject(resultStr);
            JsonObject blobJson = json;

            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(201);
            assertThat(resultEntity.getContentType().getValue()).isEqualTo(APPLICATION_JSON);

            // do some more assertions on the response
            assertThat(json.getObject("self").getString("href")).startsWith("/testOrg/testApp/gridfs/john/.files/");
            assertThat(json.getString("filename")).isEqualTo("beach.jpg");
            String blobId = json.getString("id");
            assertThat(blobId).isNotEqualTo("beach.jpg");
            assertThat(json.getNumber("length")).isEqualTo(20);

            JsonArray links = json.getArray("links");
            assertThat(links).isNotNull();
            assertThat(links.size()).isEqualTo(3);
            assertLink(links.get(0), "self", "/testOrg/testApp/gridfs/john/vacation/italy_2013/beach.jpg;meta");
            assertLink(links.get(1), "parent", "/testOrg/testApp/gridfs/john/vacation/italy_2013");
            assertLink(links.get(2), "blob", "/testOrg/testApp/gridfs/john/vacation/italy_2013/beach.jpg");



            // get blobs root for john
            HttpGet get = new HttpGet("http://localhost:8080/testOrg/testApp/gridfs/john/.files?fields=" + URLEncoder.encode("*(*(*))", "utf-8"));
            get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

            System.err.println("DO GET - " + get.getURI());
            result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            resultEntity = result.getEntity();
            assertThat(resultEntity.getContentLength()).isGreaterThan(0);
            baos = new ByteArrayOutputStream();
            resultEntity.writeTo(baos);

            resultStr = new String(baos.toByteArray());
            System.err.println(resultStr);
            json = new JsonObject(resultStr);

            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(resultEntity.getContentType().getValue()).isEqualTo(APPLICATION_JSON);

            // do some more assertions on the response
            assertThat(json.getObject("self").getString("href")).isEqualTo("/testOrg/testApp/gridfs/john/.files");
            assertThat(json.getString("id")).isNotEqualTo(".blobs");
            assertThat(json.getBoolean("dir")).isEqualTo(true);
            // TODO add owner, and createDate

            links = json.getArray("links");
            assertThat(links).isNotNull();
            assertThat(links.size()).isEqualTo(2);
            assertLink(links.get(0), "self", "/testOrg/testApp/gridfs/john/.files");
            assertLink(links.get(1), "parent", "/testOrg/testApp/gridfs/john");

            JsonArray members = json.getArray("_members");
            assertThat(members).isNotNull();
            assertThat(members.size()).isEqualTo(4);

            assertBlobItem(members.get(3), blobId);



            // get files root for john
            get = new HttpGet("http://localhost:8080/testOrg/testApp/gridfs/john/.files?fields=" + URLEncoder.encode("*(*(*))", "utf-8"));
            get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

            System.err.println("DO GET - " + get.getURI());
            result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            resultEntity = result.getEntity();
            if (resultEntity.getContentLength() > 0) {
                resultEntity.writeTo(System.err);
            }
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(resultEntity.getContentType().getValue()).isEqualTo(APPLICATION_JSON);



            // get gridfs root - it returns name and version
            get = new HttpGet("http://localhost:8080/testOrg/testApp/gridfs");
            get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

            System.err.println("DO GET - " + get.getURI());
            result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            resultEntity = result.getEntity();
            if (resultEntity.getContentLength() > 0) {
                resultEntity.writeTo(System.err);
            }
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(resultEntity.getContentType().getValue()).isEqualTo(APPLICATION_JSON);



            // get root root for john
            get = new HttpGet("http://localhost:8080/testOrg/testApp/gridfs/john?fields=" + URLEncoder.encode("*(*(*))", "utf-8"));
            get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

            System.err.println("DO GET - " + get.getURI());
            result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            resultEntity = result.getEntity();
            if (resultEntity.getContentLength() > 0) {
                resultEntity.writeTo(System.err);
            }
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(resultEntity.getContentType().getValue()).isEqualTo(APPLICATION_JSON);



            // now read the blob
            get = new HttpGet("http://localhost:8080/testOrg/testApp/gridfs/john/vacation/italy_2013/beach.jpg");
            get.setHeader(HttpHeaders.Names.ACCEPT, ALL);

            System.err.println("DO GET - " + get.getURI());
            result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            resultEntity = result.getEntity();
            if (resultEntity.getContentLength() > 0) {
                resultEntity.writeTo(System.err);
            }
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(resultEntity.getContentLength()).isEqualTo(content.length());
            assertThat(resultEntity.getContentType().getValue()).isEqualTo(BLOB_CONTENT_TYPE);

            // and again via blob uri
            get = new HttpGet("http://localhost:8080" + blobJson.getObject("self").getString("href").replace(".files", ".blobs"));
            get.setHeader(HttpHeaders.Names.ACCEPT, ALL);

            System.err.println("DO GET - " + get.getURI());
            result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            resultEntity = result.getEntity();
            if (resultEntity.getContentLength() > 0) {
                resultEntity.writeTo(System.err);
            }
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(resultEntity.getContentLength()).isEqualTo(content.length());
            assertThat(resultEntity.getContentType().getValue()).isEqualTo(BLOB_CONTENT_TYPE);



            // get file meta info directly
            get = new HttpGet("http://localhost:8080/testOrg/testApp/gridfs/john/vacation/italy_2013/beach.jpg;meta");
            get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

            System.err.println("DO GET - " + get.getURI());
            result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            resultEntity = result.getEntity();
            if (resultEntity.getContentLength() > 0) {
                resultEntity.writeTo(System.err);
            }
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(resultEntity.getContentType().getValue()).isEqualTo(APPLICATION_JSON);



            // get files in directory
            get = new HttpGet("http://localhost:8080/testOrg/testApp/gridfs/john/vacation/italy_2013");
            get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

            System.err.println("DO GET - " + get.getURI());
            result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            resultEntity = result.getEntity();
            if (resultEntity.getContentLength() > 0) {
                resultEntity.writeTo(System.err);
            }
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(resultEntity.getContentType().getValue()).isEqualTo(APPLICATION_JSON);



            // get expanded file infos of directory children
            get = new HttpGet("http://localhost:8080/testOrg/testApp/gridfs/john/vacation/italy_2013?fields=" + URLEncoder.encode("*(*(*))", "utf-8"));
            get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

            System.err.println("DO GET - " + get.getURI());
            result = httpClient.execute(get);
            System.err.println("=============>>>");
            System.err.println(result);

            resultEntity = result.getEntity();
            if (resultEntity.getContentLength() > 0) {
                resultEntity.writeTo(System.err);
            }
            System.err.println("\n<<<=============");
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(resultEntity.getContentType().getValue()).isEqualTo(APPLICATION_JSON);
        } finally {
            httpClient.close();
        }
    }

    private void assertBlobItem(Object item, String id) {
        assertThat(item).isInstanceOf(JsonObject.class);
        JsonObject obj = (JsonObject) item;
        assertThat(obj.getString("id")).isEqualTo(id);
        assertThat(obj.getObject("self").getString("href")).isEqualTo("/testOrg/testApp/gridfs/john/.files/" + id);
        assertThat(obj.getString("filename")).isEqualTo("beach.jpg");
    }
}
