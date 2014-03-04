package org.liveoak.testsuite.internal.server;

import org.liveoak.testsuite.internal.Config;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * LiveOak server based on standalone JVM process
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class StandaloneProcessLiveOakServer extends LiveOakServer {

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

    @Override
    protected void startImpl(Map<String, String> jvmProperties, String[] jvmArguments) throws Exception {
        if (liveoak != null) {
            throw new IllegalStateException("Already running");
        }

        long time = System.currentTimeMillis();

        String java = path(System.getProperty("java.home"), "bin", "java");

        String liveoakDir = getLiveOakDir();

        String modulesJar = path(liveoakDir, "launcher", "target", "jboss-modules.jar");

        List<String> cmd = new LinkedList<>();
        cmd.add(java);

        for (Map.Entry<String, String> property : jvmProperties.entrySet()) {
            cmd.add("-D" + property.getKey() + "=" + property.getValue());
        }

        cmd.add("-jar");
        cmd.add(modulesJar);

        for (String argument : jvmArguments) {
            cmd.add(argument);
        }

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

    private void kill() {
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

    private boolean isAlive() {
        return liveoak != null && liveoak.isAlive();
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
