/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.keycloak;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.keycloak.models.Config;
import org.keycloak.models.utils.ModelProviderUtils;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class AbstractKeycloakTest extends AbstractResourceTestCase {

    protected ObjectNode createTestConfig() {
        ObjectNode config = JsonNodeFactory.instance.objectNode();

        String requestedModel = Config.getModelProvider();
        if (requestedModel != null) {
            config.put(KeycloakSystemResource.MODEL, requestedModel);
        }

        return config;
    }
}
