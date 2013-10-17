/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.projectodd.restafari.mongo.unit;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.projectodd.restafari.mongo.ServerStarterVerticle;
import org.junit.Test;


public class ExampleUnitTest {

    @Test
    public void testVerticle() throws Exception {
        /*
        ServerStarterVerticle vert = new ServerStarterVerticle();
        vert.start();

        try {
            HttpGet get = new HttpGet("http://localhost:8080/storage");
            get.setHeader("Content-Type", "application/json");
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            CloseableHttpResponse response = httpClient.execute(get);

            Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        } finally {
            vert.stop();
        }
        */
    }
}
