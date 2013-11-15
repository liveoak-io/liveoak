/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.vertx.modules.server.integration.java;

import org.junit.Test;
import org.junit.runner.RunWith;
import io.liveoak.vertx.testtools.VertxTestRunner;
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
