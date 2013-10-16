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

import org.junit.Assert;
import org.projectodd.restafari.mongo.ServerStarterVerticle;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

public class ExampleUnitTest {

    @Test
    public void testVerticle() throws Exception {
        ServerStarterVerticle vert = new ServerStarterVerticle();
        vert.start();

        try {
            URL url = new URL("http://localhost:8080/storage");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            Assert.assertEquals(404, con.getResponseCode());
        } finally {
            vert.stop();
        }
    }
}
