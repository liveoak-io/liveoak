/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.launcher.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import io.liveoak.mongo.launcher.MongoLauncher;
import io.liveoak.mongo.launcher.config.MongoLauncherConfigResource;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MongoLauncherService implements Service<MongoLauncher> {

    private static final Logger log = Logger.getLogger("mongo-launcher");

    private final String id;
    private MongoLauncher launcher;
    private InjectedValue<MongoLauncherConfigResource> configResourceInjector = new InjectedValue<>();

    public MongoLauncherService(String id) {
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {

        boolean detectRunning;

        // init launcher properties
        MongoLauncherConfigResource config = configResourceInjector.getValue();
        switch(config.enabled()) {
            case "auto":
                detectRunning = true;
                break;
            case "true":
                detectRunning = false;
                break;
            case "false":
                log.info("MongoLauncher is explicitly disabled. Not starting mongod automatically");
                return;
            default:
                log.warn("Not starting mongod automatically - configuration parameter 'enabled' contains invalid value: " + config.enabled());
                return;
        }

        launcher = new MongoLauncher();

        int port = config.port();
        if (port != 0) {
            launcher.setPort(port);
        }

        if (serverRunning(port)) {
            log.warn("It appears there is an existing server on port: " + port);
            if (detectRunning) {
                log.warn("Not starting mongod (you can change this behavior via 'enabled' property in conf/extensions/mongo-launcher.json)");
                return;
            } else {
                throw new RuntimeException("Can't start mongod (port already in use)");
            }
        }

        String val = config.mongodPath();
        if (val != null) {
            launcher.setMongodPath(val);
        }

        val = config.dbPath();
        if (val != null) {
            launcher.setDbPath(val);
        }

        val = config.logPath();
        if (val != null) {
            launcher.setLogPath(val);
        }

        val = config.pidFilePath();
        if (val != null) {
            launcher.setPidFilePath(val);
        }

        launcher.setUseSmallFiles(config.useSmallFiles());

        val = config.extraArgs();
        if (val != null) {
            launcher.addExtraArgs(val);
        }

        launcher.setUseAnyMongod(config.useAnyMongod());

        try {
            launcher.startMongo();

            while(!serverRunning(port)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }

            log.info("Mongo started");
        } catch (IOException e) {
            throw new StartException("Failed to start MongoDB - Check mongo-launcher.json extension configuration file", e);
        }
    }

    private boolean serverRunning(int port) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", port), 500);
            if (socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                    log.debug("[IGNORED] Exception closing server test socket: ", ignored);
                }
                return true;
            }
        } catch (IOException ignored) {
            log.debug("[DEBUG] Connection test: ", ignored);
        }
        return false;
    }

    @Override
    public void stop(StopContext context) {
        try {
            if (launcher != null) {
                launcher.stopMongo();
            }
        } catch (IOException e) {
            log.warn("Failed to stop MongoDB: ", e);
        }
    }

    @Override
    public MongoLauncher getValue() throws IllegalStateException, IllegalArgumentException {
        return launcher;
    }

    public InjectedValue<MongoLauncherConfigResource> configResourceInjector() {
        return this.configResourceInjector;
    }
}
