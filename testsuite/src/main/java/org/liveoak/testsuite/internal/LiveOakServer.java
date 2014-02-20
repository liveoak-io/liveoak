package org.liveoak.testsuite.internal;

import org.jboss.logging.Logger;
import org.liveoak.testsuite.annotations.LiveOakConfig;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class LiveOakServer {

    private static final Logger log = Logger.getLogger(LiveOakServer.class);

    private static Process liveoak;
    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (liveoak != null) {
                    liveoak.destroyForcibly();
                    liveoak = null;
                }
            }
        });
    }

    private String config;
    private String app;

    public LiveOakServer() {
        config = "default";
        app = "default";
    }

    public LiveOakServer(LiveOakConfig config) {
        this.config = config.config();
        this.app = config.app();
    }

    public void start() throws Exception {
        if (liveoak != null) {
            throw new IllegalStateException("Already running");
        }

        long time = System.currentTimeMillis();

        String java = path(System.getProperty("java.home"), "bin", "java");

        File t = new File("").getAbsoluteFile();
        if (t.getName().equals("testsuite")) {
            t = t.getParentFile();
        }
        String liveoakDir = t.getAbsolutePath();

        String jsClientDir = path(liveoakDir, "clients", "javascript", "src", "main", "javascript");
        String cssDir = path(liveoakDir, "launcher", "src", "main", "css");

        String modulesJar = path(liveoakDir, "launcher", "target", "jboss-modules.jar");
        String modulePath = path(liveoakDir, "modules", "target", "modules");

        String testDir = path(liveoakDir, "testsuite", "src", "test");

        String configDir = path(testDir, "test-configurations", config);
        String appDir = path(testDir, "test-apps", app);

        List<String> cmd = new LinkedList<>();
        cmd.add(java);
        cmd.add("-Dio.liveoak.js.dir=" + jsClientDir);
        cmd.add("-Dcss.dir=" + cssDir);
        cmd.add("-jar");
        cmd.add(modulesJar);
        cmd.add("-modulepath");
        cmd.add(modulePath);
        cmd.add("io.liveoak.bootstrap:main");
        cmd.add(configDir);
        cmd.add(appDir);

        //ProcessBuilder pb = new ProcessBuilder().directory(new File(appDir)).command(cmd);
        ProcessBuilder pb = new ProcessBuilder().command(cmd);

        //if (Config.showOutput()) {
            pb.inheritIO();
        //}

        liveoak = pb.start();

        waitFor("http://localhost:8080", Config.startTimeout());

        //waitFor("http://localhost:8383/auth-server/rest/realms/default", Config.stopTimeout());

        log.info("Started LiveOakServer in " + (System.currentTimeMillis() - time) + " ms");

    }

    public void stop() {
        if (liveoak == null) {
            throw new IllegalStateException("Not running");
        }

        long time = System.currentTimeMillis();

        liveoak.destroy();

        try {
            if (liveoak.waitFor(Config.stopTimeout(), TimeUnit.MILLISECONDS)) {
                log.info("Stopped LiveOakServer in " + (System.currentTimeMillis() - time) + " ms");
                liveoak = null;
            }
        } catch (InterruptedException e) {
        }

        if (liveoak != null) {
            log.warn("LiveOakServer did not stop within timeout");
            kill();
        }
    }

    public void kill() {
        if (liveoak == null) {
            throw new IllegalStateException("Not running");
        }

        liveoak.destroyForcibly();

        if (liveoak.isAlive()) {
            log.error("Failed to kill LiveOakServer");
        } else {
            log.info("Killed LiveOakServer");
        }
    }

    public boolean isAlive() {
        return liveoak != null && liveoak.isAlive();
    }

    private static String path(String parent, String... path) {
        StringBuilder sb = new StringBuilder(parent);
        for (String p : path) {
            sb.append(File.separator);
            sb.append(p);
        }
        return sb.toString();
    }

    private static void waitFor(String url, long timeout) throws MalformedURLException, InterruptedException {
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
