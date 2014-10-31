/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.keycloak;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.testtools.AbstractTestCaseWithTestApp;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractKeycloakTest extends AbstractTestCaseWithTestApp {

    protected static ObjectNode createTestConfig() {
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.put("keycloak-url", "http://localhost:8383/auth");
        config.put("load-public-keys", false);

        ObjectNode keys = JsonNodeFactory.instance.objectNode();
        keys.put("liveoak-apps", TokenUtil.PUBLIC_KEY_PEM);

        config.put("public-keys", keys);

        return config;
    }
}
