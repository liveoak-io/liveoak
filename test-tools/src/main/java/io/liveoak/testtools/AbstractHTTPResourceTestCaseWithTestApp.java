/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.testtools;

import org.junit.BeforeClass;

/**
 * @author Ken Finnigan
 */
public abstract class AbstractHTTPResourceTestCaseWithTestApp extends AbstractHTTPResourceTestCase {
    @BeforeClass
    public static void setupTestApp() throws Exception {
        installTestApp();
    }
}
