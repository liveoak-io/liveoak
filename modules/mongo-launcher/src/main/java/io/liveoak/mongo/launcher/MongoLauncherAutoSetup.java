/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.launcher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ObjectMapperFactory;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.liveoak.mongo.launcher.MongoLauncher.nullOrEmpty;
import static io.liveoak.mongo.launcher.config.Constants.DB_PATH;
import static io.liveoak.mongo.launcher.config.Constants.ENABLED;
import static io.liveoak.mongo.launcher.config.Constants.LOG_PATH;
import static io.liveoak.mongo.launcher.config.Constants.MONGOD_PATH;
import static io.liveoak.mongo.launcher.config.Constants.PID_FILE_PATH;
import static io.liveoak.mongo.launcher.config.Constants.USE_SMALL_FILES;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MongoLauncherAutoSetup {

    private static final Logger log = Logger.getLogger("mongo-launcher");

    public static void setup(File liveoakDir, File confDir) {

        System.err.println( "** SETTING UP MONGO" );

        try {
            liveoakDir = liveoakDir.getCanonicalFile();
        } catch (IOException ignored) {}

        try {
            confDir = confDir.getCanonicalFile();
        } catch (IOException ignored) {}

        // locate mongoLauncher.json extension config
        File extDir = new File(confDir, "extensions");
        File launcherJson = new File(extDir, "mongo-launcher.json");

        // we expect it to be present. If it's not present it means it is first-time setup
        // in that case we restore it
        ObjectMapper mapper = ObjectMapperFactory.create();

        ObjectNode extension;
        if (!launcherJson.isFile()) {
            extension = defaultConfig(liveoakDir);
            // marshal it to file
            try {
                mapper.writeValue(launcherJson, extension);
                log.info("Created config file: " + launcherJson.getAbsolutePath());
            } catch (IOException e) {
                log.info("[IGNORED] Failed to restore: " + launcherJson.getAbsolutePath(), e);
                log.info("MongoDB will not be started automatically");
            }
        } else {
            // load config from file
            try {
                extension = (ObjectNode) mapper.readTree(launcherJson);

                // if exists peek inside its config for flag enabled: false
                // if set, report the fact and return
                JsonNode config = extension.get("config");
                if (config != null) {
                    JsonNode value = config.get(ENABLED);
                    if (value != null && "false".equals(value.asText())) {
                        log.info("MongoDB auto-start has been disabled in mongo-launcher.json - make sure to manually start MongoDB on appropriate port");
                        return;
                    }
                }
            } catch (IOException e) {
                log.error("Failed to read configuration: " + launcherJson.getAbsolutePath());
                throw new RuntimeException("Failed to read configuration: " + launcherJson.getAbsolutePath(), e);
            }
        }

        // the default config has useAnyMongod set, and doesn't care about mongod version
        // check if mongodPath is set - that means user has set it
        ObjectNode config = (ObjectNode) extension.get("config");
        JsonNode mongodPath = config.get(MONGOD_PATH);

        if (mongodPath != null)  {
            // check that mongod executes
            try {
                OsUtils.execWithOneLiner(new String[]{mongodPath.asText(), "--version"}, null, false);
            } catch (Exception e) {
                log.error("Configured mongod failed to execute: ", e);
                return;
            }
        }
        if (mongodPath == null) {
            // if not, set it, let's try to locate existing mongod on the system, and try to execute it
            String path = MongoLauncher.findMongod();
            if (nullOrEmpty(path)) {
                // if no mongod found, launch installer to get it
                // set path to it in mongo-launcher.json
                MongoInstaller installer = new MongoInstaller();
                // use ~/.liveoak/mongo for downloaded archives
                Path mongoDir = Paths.get(System.getProperty("user.home"), ".liveoak", "mongo");
                try {
                    if (!Files.isDirectory(mongoDir)) {
                        Files.createDirectories(mongoDir);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create directory: " + mongoDir);
                }

                installer.setTempDir(mongoDir.toString());
                installer.setInstallDir(mongoDir.toString());
                try {
                    installer.performInstall();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("MongoDB installation failed: ", e);
                } catch (Throwable t) {
                    t.printStackTrace();
                    throw t;
                }

                // mongo should be installed under mongoDir
                path = installer.getMongodPath();

                if (nullOrEmpty(path)) {
                    throw new RuntimeException("Failed to properly install MongoDB");
                }
            }

            // if success set path in mongo-launcher.json
            // determine mongod version
            String mongoVersion = OsUtils.execWithOneLiner(new String[] {path, "--version"}, null, false);

            if (!nullOrEmpty(mongoVersion)) {
                try {
                    config.put(MONGOD_PATH, path);
                    mapper.writeValue(launcherJson, extension);
                } catch (IOException e) {
                    log.error("Failed to update: " + launcherJson.getAbsolutePath(), e);
                    log.error("MongoDB will not be started automatically");
                }
            } else {
                log.error("Failed to find / install a working mongod");
                log.info("MongoDB will not be started automatically");
            }
        }
    }

    private static ObjectNode defaultConfig(File liveoakDir) {
        // make sure data dir exists
        File dataDir = new File(liveoakDir, "data");
        if (!dataDir.isDirectory()) {
            try {
                Files.createDirectory(dataDir.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to create data directory: " + dataDir.getAbsolutePath(), e);
            }
        }

        ObjectNode result = JsonNodeFactory.instance.objectNode();
        result.put("module-id", "io.liveoak.mongo-launcher");
        ObjectNode config = result.putObject("config");

        config.put(ENABLED, "auto");
        config.put(LOG_PATH, new File(dataDir, "mongod.log").getAbsolutePath());
        config.put(PID_FILE_PATH, new File(dataDir, "mongod.pid").getAbsolutePath());
        config.put(DB_PATH, dataDir.getAbsolutePath());
        config.put(USE_SMALL_FILES, true);

        return result;
    }
}
