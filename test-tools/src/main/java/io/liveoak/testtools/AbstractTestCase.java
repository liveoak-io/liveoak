/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.testtools;

import io.liveoak.mongo.launcher.MongoInstaller;
import io.liveoak.mongo.launcher.MongoLauncher;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Random;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class AbstractTestCase {
    protected File projectRoot;

    protected static MongoLauncher mongoLauncher;
    protected static int mongoPort = 27017;
    protected static String mongoHost = "localhost";

    @BeforeClass
    public static void setupMongo() throws IOException {
        if (mongoLauncher != null) {
            throw new IllegalStateException("Assertion failed: static mongoLauncher is not null");
        }
        String host = System.getProperty("mongo.host");
        String port = System.getProperty("mongo.port");
        String moduleDir = System.getProperty("user.dir");

        if (host != null && ("localhost".equals(host) || "127.0.0.1".equals(host))) {

            // make sure mongod is installed
            String mongodPath = MongoInstaller.autoInstall();

            if (mongodPath == null) {
                throw new RuntimeException("Failed to install MongoDB!");
            }

            // launch mongod
            mongoLauncher = new MongoLauncher();
            mongoLauncher.setMongodPath(mongodPath);
            mongoLauncher.setUseSmallFiles(true);

            if (port != null) {
                mongoPort = Integer.parseInt(port);
            }
            mongoLauncher.setPort( mongoPort );

            if (host != null) {
                mongoHost = host;
            }
            mongoLauncher.setHost( mongoHost );

            String dataDir = new File(moduleDir, "target/data_" + randomName()).getAbsolutePath();
            File ddFile = new File(dataDir);
            if (!ddFile.isDirectory()) {
                if (!ddFile.mkdirs()) {
                    throw new RuntimeException("Failed to create a data directory: " + dataDir);
                }
            }
            String logFile = new File(dataDir, "mongod.log").getAbsolutePath();
            mongoLauncher.setDbPath(dataDir);
            mongoLauncher.setLogPath(logFile);
            mongoLauncher.setPidFilePath(new File(ddFile, "mongod.pid").getAbsolutePath());
            mongoLauncher.startMongo();

            // wait for it to start
            long start = System.currentTimeMillis();
            while(!mongoLauncher.serverRunning(mongoHost, mongoPort, (e) -> {
                    if (System.currentTimeMillis() - start > 120000) throw new RuntimeException(e);
                })) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted!");
                }
            }
        }
    }

    private static String randomName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            int num = (int) (35 * Math.random());
            if (num > 9) {
                sb.append((char) ('a' + num - 9));
            } else {
                sb.append((char) ('0' + num));
            }
        }
        return sb.toString();
    }

    @Before
    public void setupUserDir() {
        String name = getClass().getName().replace(".", "/") + ".class";
        URL resource = getClass().getClassLoader().getResource(name);

        if (resource != null) {
            File current = new File(resource.getFile());

            while (current.exists()) {
                if (current.isDirectory()) {
                    if (new File(current, "pom.xml").exists()) {
                        this.projectRoot = current;
                        break;
                    }
                }

                current = current.getParentFile();
            }
        }

        if (this.projectRoot != null) {
            System.setProperty("user.dir", this.projectRoot.getAbsolutePath());
        }
    }

    @AfterClass
    public static void tearDownMongo() throws IOException {
        if (mongoLauncher != null) {
            mongoLauncher.stopMongo();

            // wait for it to stop
            long start = System.currentTimeMillis();
            while(mongoLauncher.serverRunning(mongoHost, mongoPort, (e) -> { if (System.currentTimeMillis() - start > 120000) throw new RuntimeException(e); })) {

                if (System.currentTimeMillis() - start > 120000) {
                    throw new RuntimeException("mongod process still seems to be running (2m timeout)");
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted!");
                }
            }

            // now delete the data dir except log file
            Files.walkFileTree(new File(mongoLauncher.getDbPath()).toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!file.startsWith(mongoLauncher.getLogPath())) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    try {
                        Files.delete(dir);
                    } catch (DirectoryNotEmptyException ignored) {
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            mongoLauncher = null;
        }
    }
}
