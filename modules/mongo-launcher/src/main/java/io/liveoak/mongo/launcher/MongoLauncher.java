/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MongoLauncher {

    private static final Logger log = Logger.getLogger("mongo-launcher");
    private static final Logger mongoLog = Logger.getLogger("mongod");

    private String pidFilePath;
    private int port;
    private String mongodPath;
    private boolean useAnyMongod;
    private String dbPath;
    private String logPath;
    private OutputStream stdin;
    private Process process;

    public String getPidFilePath() {
        return pidFilePath;
    }

    public void setPidFilePath(String pidFilePath) {
        this.pidFilePath = pidFilePath;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMongodPath() {
        return mongodPath;
    }

    public void setMongodPath(String mongodPath) {
        this.mongodPath = mongodPath;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public void setUseAnyMongod(boolean useAny) {
        this.useAnyMongod = useAny;
    }

    public boolean getUseAnyMongod() {
        return useAnyMongod;
    }

    public void startMongo() throws IOException {

        StringBuilder sb = new StringBuilder();
        String mongoVersion = "unknown version";
        if (nullOrEmpty(mongodPath)) {
            if (useAnyMongod) {
                mongodPath = findMongod();
                if (nullOrEmpty(mongodPath)) {
                    throw new RuntimeException("No path to mongod specified, and failed to find one on the system");
                }
            } else {
                throw new RuntimeException("No path to mongod specified");
            }
        }
        sb.append(mongodPath).append(" ");

        if (notNullOrEmpty(pidFilePath)) {
            sb.append(" --pidfilepath " + pidFilePath);
        }

        if (notNullOrEmpty(dbPath)) {
            sb.append(" --dbpath " + dbPath);
        }

        if (notNullOrEmpty(logPath)) {
            sb.append(" --logpath " + logPath);
        }

        if (port != 0) {
            sb.append(" --port " + port);
        }

        log.info("Starting mongod: " + sb);
        process = Runtime.getRuntime().exec(sb.toString());

        // stdout
        InputStream stdout = process.getInputStream();
        InputStream stderr = process.getErrorStream();
        stdin = process.getOutputStream();

        new ReaderThread(
                new BufferedReader(new InputStreamReader(stdout))
        ).start();

        new ReaderThread(
                new BufferedReader(new InputStreamReader(stderr))
        ).start();
    }

    public static String findMongod() {
        try {
            String result = null;

            OsArch osArch = OsUtils.determineOSAndArch();
            if (osArch.isWindows()) {
                // first look at $HOME/.liveoak/mongo if there is a mongo installation available
                result = findMongodInHome("mongod.exe");
                if (result == null) {
                    try {
                        result = OsUtils.execWithResult(new String[]{"where", "mongod"}, null, false);
                    } catch (Exception ignored) {
                        log.debug("[IGNORED] Command execution failed: where mongod", ignored);
                    }
                }
            } else {
                // first look at $HOME/.liveoak/mongo if there is a mongo installation available
                result = findMongodInHome("mongod");
                if (result == null) {
                    try {
                        result = OsUtils.execWithOneLiner(new String[] {"which", "mongod"}, null, false);
                    } catch (Exception ignored) {
                        log.debug("[IGNORED] Command execution failed: which mongod", ignored);
                    }
                    if (nullOrEmpty(result)) {
                        try {
                            result = OsUtils.execWithOneLiner(new String[] {"sh", "-c", "locate mongod | grep mongod$"}, null, false);
                        } catch (Exception ignored) {
                            log.debug("[IGNORED] Command execution failed: sh -c 'locate mongod | grep mongod$'", ignored);
                        }
                    }
                }
            }

            if (result != null) {
                try {
                    result = new File(result).getCanonicalPath();
                } catch (IOException ignored) {
                    log.debug("[IGNORED] Failed to make path canonical: " + result);
                }
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to find mongod: ", e);
        }
    }

    private static String findMongodInHome(String mongodName) {
        Path home = Paths.get(System.getProperty("user.home"));
        Path mongo = Paths.get(home.toString(), ".liveoak", "mongo");
        if (mongo.toFile().isDirectory()) {
            // list any subdirectory until you find one containing bin/mongod
            for (File item: mongo.toFile().listFiles()) {
                if (item.isDirectory()) {
                    File mongod = Paths.get(item.getAbsolutePath(), "bin", mongodName).toFile();
                    if (mongod.isFile()) {
                        return mongod.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    public boolean started() {
        return process != null && process.isAlive();
    }

    static boolean notNullOrEmpty(String value) {
        return !nullOrEmpty(value);
    }

    static boolean nullOrEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    public void stopMongo() throws IOException {
        if (stdin != null) {
            stdin.close();
        }
        if (process != null) {
            process.destroy();
        }
    }

    static class ReaderThread extends Thread {

        private final BufferedReader reader;

        ReaderThread(BufferedReader reader) {
            this.reader = reader;
        }

        public void run() {

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    mongoLog.info(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String [] args) throws IOException {
        MongoLauncher mongo = new MongoLauncher();
        String home = System.getProperty("user.home");
        File dbFile = new File(home, "mongo-data");
        if (!dbFile.isDirectory() && !dbFile.mkdirs()) {
            throw new IOException("Failed to create db data directory: " + dbFile);
        }
        mongo.setDbPath(dbFile.getAbsolutePath());
        mongo.setMongodPath("/usr/local/bin/mongod");
        mongo.setPidFilePath(new File(home, "mongod.pid").getAbsolutePath());
        mongo.setLogPath(new File(home, "mongod.log").getAbsolutePath());
        mongo.setPort(27017);
        mongo.startMongo();

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mongo.stopMongo();
    }
}
