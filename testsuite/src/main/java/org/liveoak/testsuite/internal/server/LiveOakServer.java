/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.liveoak.testsuite.internal.server;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.liveoak.testsuite.annotations.LiveOakConfig;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class LiveOakServer {

    protected final Logger log = Logger.getLogger(getClass());

    protected String config;
    protected String app;

    protected Map<String, String> jvmProperties = new HashMap<>();
    protected String[] jvmArguments;

    public LiveOakServer() {
        config = "default";
        app = "default";
    }

    public void setConfig(LiveOakConfig config) {
        this.config = config.config();
        this.app = config.app();
    }

    public void start() throws Throwable {
        File t = new File("").getAbsoluteFile();
        if (t.getName().equals("testsuite")) {
            t = t.getParentFile();
        }
        String liveoakDir = t.getAbsolutePath();

        String jsClientDir = path(liveoakDir, "clients", "javascript", "src", "main", "javascript");
        String cssDir = path(liveoakDir, "launcher", "src", "main", "css");

        String modulePath = path(liveoakDir, "modules", "target", "modules");

        String testDir = path(liveoakDir, "testsuite", "src", "test");

        String configDir = path(testDir, "test-configurations", config);
        String appDir = path(testDir, "test-apps", app);

        String logDir = path(liveoakDir, "launcher", "logs");
        String loggingPropertiesPath = path(liveoakDir, "launcher", "etc", "logging.properties");

        jvmProperties.put("io.liveoak.js.dir", jsClientDir);
        jvmProperties.put("io.liveoak.css.dir", cssDir);
        jvmProperties.put("io.liveoak.log", logDir);
        jvmProperties.put("logging.configuration", loggingPropertiesPath);

        jvmArguments = new String[] { "-modulepath", modulePath, "io.liveoak.bootstrap:main", configDir, appDir };

        startImpl();
    }

    protected abstract void startImpl() throws Throwable;

    public abstract void stop() throws Exception;

    protected static String path(String parent, String... path) {
        StringBuilder sb = new StringBuilder(parent);
        for (String p : path) {
            sb.append(File.separator);
            sb.append(p);
        }
        return sb.toString();
    }

    protected static void waitFor(String url, long timeout) throws MalformedURLException, InterruptedException {
        long end = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < end) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(500);
                connection.setReadTimeout(500);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                if (connection.getResponseCode() == 200) {
                    return;
                }
            } catch (IOException e) {
            }
            Thread.sleep(500);
        }
        throw new RuntimeException("LiveOakServer did not start within timeout");
    }
}
