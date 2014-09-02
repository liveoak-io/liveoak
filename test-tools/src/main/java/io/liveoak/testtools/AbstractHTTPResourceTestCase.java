/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.testtools;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.tenancy.InternalApplicationExtension;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.state.ResourceState;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.vertx.java.core.Vertx;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Bob McWhirter
 */
public abstract class AbstractHTTPResourceTestCase extends AbstractTestCase {

    protected LiveOakSystem system;
    protected CloseableHttpClient httpClient;
    protected InternalApplication application;

    private Set<String> extensionIds = new HashSet<>();
    private Set<InternalApplicationExtension> extensions = new HashSet<>();
    protected Vertx vertx;

    public abstract void loadExtensions() throws Exception;

    protected File applicationDirectory() {
        return null;
    }

    protected void loadExtension(String id, Extension ext) throws Exception {
        this.system.extensionInstaller().load(id, ext);
        this.extensionIds.add(id);
    }

    protected void loadExtension(String id, Extension ext, ObjectNode config) throws Exception {
        ObjectNode fullConfig = JsonNodeFactory.instance.objectNode();
        fullConfig.put( "config", config );
        this.system.extensionInstaller().load(id, ext, fullConfig);
        this.extensionIds.add(id);
    }

    protected InternalApplicationExtension installResource(String extId, String resourceId, ObjectNode resourceConfig) throws Exception {
        InternalApplicationExtension appExt = this.application.extend(extId, resourceId, resourceConfig);
        this.extensions.add(appExt);
        return appExt;
    }

    protected InternalApplicationExtension installResource(String extId, String resourceId, ResourceState resourceConfig) throws Exception {
        return installResource( extId, resourceId, ConversionUtils.convert(resourceConfig) );
    }

    @Before
    public void setUpSystem() throws Exception {
        try {
            this.system = LiveOakFactory.create();
            this.vertx = this.system.vertx();

            // LIVEOAK-295 ... make sure system services have all started before performing programmatic application deployment
            this.system.awaitStability();

            this.system.applicationRegistry().createApplication(ZeroExtension.APPLICATION_ID, ZeroExtension.APPLICATION_NAME);
            this.application = this.system.applicationRegistry().createApplication("testApp", "Test Application", applicationDirectory());

            loadExtensions();

            this.system.awaitStability();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @After
    public void tearDownSystem() throws Exception {
        removeAllResources();

        this.system.awaitStability();
        this.system.stop();
    }

    public void removeAllResources() throws InterruptedException {
        for (InternalApplicationExtension extension : this.extensions) {
            extension.remove();
        }

        this.extensions.clear();
        this.application.configurationFile().delete();

        this.system.awaitStability();
    }

    @Before
    public void setUpClient() throws Exception {
        RequestConfig cconfig = RequestConfig.custom().setSocketTimeout(500000).build();
        this.httpClient = HttpClients.custom().disableContentCompression().setDefaultRequestConfig(cconfig).build();
    }

    @After
    public void tearDownClient() throws Exception {
        this.httpClient.close();
    }
}
