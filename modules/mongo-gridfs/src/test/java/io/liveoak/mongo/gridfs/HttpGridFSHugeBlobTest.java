/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import io.netty.handler.codec.http.HttpHeaders;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class HttpGridFSHugeBlobTest extends AbstractGridFSTest {

    private static final String USER_ROOT_URL = "http://localhost:8080/testApp/gridfs/john";
    private static final String ROOT_URL = "http://localhost:8080";
    private static final String FILES_URI_ROOT = "/testApp/gridfs/john/.files/";

    private JsonObject putBlob(String url, InputStream is, int size, boolean expectCreated) throws IOException {

        HttpPut put = new HttpPut(url);
        try {
            put.setHeader(HttpHeaders.Names.CONTENT_TYPE, "image/jpeg");
            put.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

            InputStreamEntity entity = new InputStreamEntity(is, size, ContentType.create("text/plain", "UTF-8"));
            put.setEntity(entity);

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

            System.err.println("\n<<<=============");
            if (expectCreated) {
                assertThat(result.getStatusLine().getStatusCode()).isEqualTo(201);
            } else {
                assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
            }
            assertThat(resultEntity.getContentType().getValue()).isEqualTo(APPLICATION_JSON);

            // do some more assertions on the response
            assertThat(json.getObject("self").getString("href")).startsWith("/testApp/gridfs/john/.files/");

            String [] urlSegments = url.split("/");
            String lastSegment = urlSegments[urlSegments.length-1];
            assertThat(json.getString("filename")).isEqualTo(lastSegment);
            String blobId = json.getString("id");
            assertThat(blobId).isNotEqualTo(lastSegment);
            assertThat(json.getNumber("length")).isEqualTo(size);

            JsonArray links = json.getArray("links");
            assertThat(links).isNotNull();
            assertThat(links.size()).isEqualTo(3);
            assertLink(links.get(0), "self", url.substring(ROOT_URL.length()) + ";meta");
            assertLink(links.get(1), "parent", getParentUri(urlSegments));
            assertLink(links.get(2), "blob", url.substring(ROOT_URL.length()));

            return json;

        } finally {
            put.releaseConnection();
        }
    }

    private String getParentUri(String[] urlSegments) {
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i < urlSegments.length-1; i++) {
            sb.append("/").append(urlSegments[i]);
        }
        return sb.toString();
    }

    /**
     *
     * @param url
     * @param is if null we expect status 404, otherwise we expect response body size to be the same as is.length()
     *           and response body crc32 to be the same as is.getCrc32()
     * @throws IOException
     */
    private void getBlob(String url, SampleInputStream is) throws IOException {

        HttpGet get = new HttpGet(url);
        try {
            get.setHeader(HttpHeaders.Names.ACCEPT, ALL);

            System.err.println("DO GET - " + get.getURI());
            CloseableHttpResponse result = httpClient.execute(get);

            System.err.println("=============>>>");
            System.err.println(result);

            if (is == null) {
                // expect 404
                assertThat(result.getStatusLine().getStatusCode()).isEqualTo(404);
                System.err.println("\n<<<=============");
                return;
            }

            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);

            HttpEntity resultEntity = result.getEntity();
            assertThat(resultEntity.getContentLength()).isGreaterThan(0);
            CountOutputStream counter = new CountOutputStream();
            resultEntity.writeTo(counter);
            System.err.println("\n... content not displayed ...\n<<<=============");

            assertThat(counter.getCount()).isEqualTo(is.getSize());
            assertThat(resultEntity.getContentType().getValue()).isEqualTo("image/jpeg");
            assertThat(is.getCrc32()).isEqualTo(counter.getCrc32());
        } finally {
            get.releaseConnection();
        }
    }

    private void deleteBlob() throws IOException {
        HttpDelete get = new HttpDelete("http://localhost:8080/testApp/gridfs/john/vacation/mars_2038/beach.jpg");
        try {
            get.setHeader(HttpHeaders.Names.ACCEPT, ALL);

            System.err.println("DO DELETE - " + get.getURI());
            CloseableHttpResponse result = httpClient.execute(get);

            System.err.println("=============>>>");
            System.err.println(result);
            System.err.println("\n<<<=============");

            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
        } finally {
            get.releaseConnection();
        }
    }

    private JsonObject putFileMeta(String url, JsonObject json) throws IOException {
        HttpPut put = new HttpPut(url);
        try {
            put.setHeader(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
            put.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

            StringEntity entity = new StringEntity(json.toString(), "utf-8");
            put.setEntity(entity);

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
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(resultEntity.getContentType().getValue()).isEqualTo(APPLICATION_JSON);

            return json;
        } finally {
            put.releaseConnection();
        }
    }

    private JsonObject getFileMeta(String url, int expectStatus) throws IOException {
        HttpGet get = new HttpGet(url);
        try {
            get.setHeader(HttpHeaders.Names.ACCEPT, APPLICATION_JSON);

            System.err.println("DO GET - " + get.getURI());
            CloseableHttpResponse result = httpClient.execute(get);

            System.err.println("=============>>>");
            System.err.println(result);

            HttpEntity resultEntity = result.getEntity();

            if (expectStatus == 404) {
                System.err.println("\n<<<=============");
                assertThat(result.getStatusLine().getStatusCode()).isEqualTo(404);
                //TODO: check that the results body contians the proper error message.
                //assertThat(resultEntity.getContentLength()).isEqualTo(0);
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resultEntity.writeTo(baos);

            String resultStr = new String(baos.toByteArray());
            System.err.println(resultStr);
            JsonObject json = new JsonObject(resultStr);

            System.err.println("\n<<<=============");

            assertThat(resultEntity.getContentLength()).isGreaterThan(0);
            assertThat(result.getStatusLine().getStatusCode()).isEqualTo(200);
            assertThat(resultEntity.getContentType().getValue()).isEqualTo(APPLICATION_JSON);
            // do some more assertions on the response
            assertThat(json.getObject("self").getString("href")).startsWith(FILES_URI_ROOT);

            if (url.startsWith(FILES_URI_ROOT)) {
                String [] urlSegments = url.split("/");
                String lastSegment = urlSegments[urlSegments.length-1];
                String blobId = json.getString("id");
                assertThat(blobId).isEqualTo(lastSegment);
            }
            return json;
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * Test CRUD - creating a new blob, reading a blob, updating existing blob, and deleting a blob
     */
    @Test
    public void testReadWriteHugeBlob() throws Exception {

        try {
            int size = 10 * 1024 * 1024;
            SampleInputStream is = new SampleInputStream(size);

            String parentUrl = USER_ROOT_URL + "/vacation/mars_2038";
            String pathUrl = parentUrl + "/beach.jpg";

            // create
            putBlob(pathUrl, is, size, true);
            // read
            getBlob(pathUrl, is);

            // now update the same url with a different content
            size = 5 * 1024 * 1024;
            SampleInputStream is2 = new SampleInputStream(size);

            // update
            putBlob(pathUrl, is2, size, false);
            // read
            getBlob(pathUrl, is2);

            // delete
            deleteBlob();
            // read  - expect 404
            getBlob(pathUrl, null);

            // create it again
            is = new SampleInputStream(size);
            JsonObject result = putBlob(pathUrl, is, size, true);


            // now rename file to something else via .files uri, and add some tags at the same time
            String newFilename = "boat_trip.jpg";
            result.putArray("tags", new JsonArray(new Object[] {"summer", "vacation", "mars", "2038"}));
            result.putString("filename", newFilename);

            // try to manipulate immutable fields as well
            result.putNumber("length", 0L);
            JsonArray links = new JsonArray();
            JsonObject ref = new JsonObject();
            ref.putString("rel", "self");
            ref.putString("href", "must not be saved");
            links.add(ref);
            result.putArray("links", links);

            // use files url to update meta info - expect 200
            String filesUrl = ROOT_URL + result.getObject("self").getString("href");
            putFileMeta(filesUrl, result);

            // get it by old pathname - expect 404
            getBlob(pathUrl, null);

            // get it by new pathname - expect 200
            pathUrl = parentUrl + "/" + newFilename;
            getBlob(pathUrl, is);

            // get meta by same old file id - expect 200
            JsonObject json = getFileMeta(filesUrl, 200);
            assertThat(json.getString("filename")).isEqualTo(newFilename);
            assertThat(json.getNumber("length").longValue()).isEqualTo(size);

            // update file info by pathname - expect 200
            result.putArray("tags", new JsonArray(new Object[] {"summer", "vacation", "mars", "2038", "sea"}));
            result = putFileMeta(parentUrl + "/boat_trip.jpg;meta", result);

            // check that result contains a new tag
            assertThat(result.getArray("tags").contains("sea"));

            // get parent info - expect 200
            result = getFileMeta(parentUrl, 200);
            assertThat(result.getString("filename")).isEqualTo("mars_2038");

            // rename parent - expect 200
            json = new JsonObject();
            json.putString("filename", "mission_to_mars_2038");
            json = putFileMeta(parentUrl, json);

            assertThat(json.getString("filename")).isEqualTo("mission_to_mars_2038");

            // get old pathname - expect 404
            getFileMeta(parentUrl, 404);

            // get new pathname - expect 200
            parentUrl = USER_ROOT_URL + "/vacation/mission_to_mars_2038";
            json = getFileMeta(parentUrl, 200);
            assertThat(json.getString("filename")).isEqualTo("mission_to_mars_2038");
            assertThat(json.getString("id")).isEqualTo(result.getString("id"));

            // update via old files url which should be the same
            json = new JsonObject();
            json.putArray("tags", new JsonArray(new Object[] {"summer", "vacation", "mars", "2038"}));
            parentUrl = ROOT_URL + result.getObject("self").getString("href");
            putFileMeta(parentUrl, json);

            // get it, and make sure it contains tags
            json = getFileMeta(parentUrl, 200);
            assertThat(json.getArray("tags")).isNotNull();
            assertThat(json.getArray("tags").contains("2038"));

            // make sure it has exactly one member
            assertThat(json.getArray("members").size()).isEqualTo(1);


            // TODO: move under different parent
            // that's a tricky one - you can put it underneath another file, or under non-existent parent

        } finally {
            httpClient.close();
        }
    }
}
