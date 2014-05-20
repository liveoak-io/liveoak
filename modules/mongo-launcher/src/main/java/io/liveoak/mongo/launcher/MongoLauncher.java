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
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MongoLauncher {

    private static final Logger log = Logger.getLogger("mongo-launcher");
    private static final Logger mongoLog = Logger.getLogger("mongod");

    private String pidFilePath;
    private String host;
    private int port;
    private String mongodPath;
    private boolean useAnyMongod;
    private String dbPath;
    private String logPath;
    private boolean useSmallFiles = true;
    private StringBuilder extraArgs;
    private OutputStream stdin;
    private Process process;

    public String getPidFilePath() {
        return pidFilePath;
    }

    public void setPidFilePath(String pidFilePath) {
        this.pidFilePath = pidFilePath;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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

    public void setUseSmallFiles(boolean useSmallFiles) {
        this.useSmallFiles = useSmallFiles;
    }

    public boolean getUseSmallFiles() {
        return useSmallFiles;
    }

    public void addExtraArgs(String extra) {
        if (extraArgs == null) {
            extraArgs = new StringBuilder();
        }
        extraArgs.append(" ").append(extra);
    }

    public String getExtraArgs() {
        return extraArgs.toString();
    }

    public void startMongo() throws IOException {

        StringBuilder sb = new StringBuilder();

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
            ensureParent(pidFilePath);
            sb.append(" --pidfilepath " + pidFilePath);
        }

        if (notNullOrEmpty(dbPath)) {
            ensureDir(dbPath);
            sb.append(" --dbpath " + dbPath);
        }

        if (notNullOrEmpty(logPath)) {
            ensureParent(logPath);
            sb.append(" --logpath " + logPath);
        }

        if (useSmallFiles) {
            sb.append(" --smallfiles");
        }

        if (host != null) {
            sb.append(" --bind_ip " + host);
        }

        if (port != 0) {
            sb.append(" --port " + port);
        }

        if (extraArgs != null) {
            sb.append(extraArgs);
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


    private void ensureDir(String path) {
        ensureDir(new File(path));
    }

    private void ensureDir(File dir) {
        if (!dir.isDirectory()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + dir.getAbsolutePath());
            }
        }
    }

    private void ensureParent(String path) {
        ensureDir(new File(path).getParentFile());
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
                            String results = OsUtils.execWithResult(new String[] {"sh", "-c", "locate mongod | grep /mongod$"}, null, false);
                            for (String line: results.split("\\n")) {
                                line = line.trim();
                                // check that file exists since 'locate' is rarely in sync with current affairs
                                if (new File(line).isFile()) {
                                    result = line;
                                    break;
                                }
                            }
                        } catch (Exception ignored) {
                            log.debug("[IGNORED] Command execution failed: sh -c 'locate mongod | grep /mongod$'", ignored);
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
        if (process != null) {
            log.info("Stopping mongod");
            process.destroy();
            try {
                boolean exited = process.waitFor(2, TimeUnit.MINUTES);
                if (!exited) {
                    throw new RuntimeException("mongod process failed to stop (2m timeout)");
                }
            } catch (InterruptedException e) {
                throw new InterruptedIOException("Interrupted");
            }
        }
    }

    public static boolean serverRunning(String host, int port) {
        return serverRunning(host, port, null);
    }

    public static boolean serverRunning(String host, int port, Consumer<Throwable> onError) {
        try {
            Socket socket = new Socket();
            socket.connect( new InetSocketAddress( host, port ), 500 );
            if (socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                    log.debug("[IGNORED] Exception closing server test socket: ", ignored);
                }
                return true;
            }
        } catch (Throwable e) {
            if (onError != null) {
                onError.accept(e);
            } else {
                log.debug("[IGNORED] Connection test: ", e);
            }
        }
        return false;
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
