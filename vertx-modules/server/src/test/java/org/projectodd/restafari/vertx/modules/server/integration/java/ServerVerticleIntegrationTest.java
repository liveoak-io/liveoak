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
package org.projectodd.restafari.vertx.modules.server.integration.java;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.projectodd.restafari.vertx.testtools.VertxTestRunner;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;
import static org.vertx.testtools.VertxAssert.assertEquals;

/**
 * Example Java integration test that deploys the module that this project builds.
 * <p/>
 * Quite often in integration tests you want to deploy the same module for all tests and you don't want tests
 * to start before the module has been deployed.
 * <p/>
 * This test demonstrates how to do that.
 */
@RunWith(VertxTestRunner.class)
public class ServerVerticleIntegrationTest extends TestVerticle {

    @Test
    public void testServerUp() {
        testComplete();
    }

    @Override
    public void start() {
        initialize();

        container.deployModule(System.getProperty("vertx.modulename"), (result) -> {
            assertTrue(result.succeeded());
            assertNotNull("deploymentID should not be null", result.result());
            startTests();
        });
    }

}
