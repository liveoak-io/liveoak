package io.liveoak.testtools;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.container.LiveOakFactory;
import io.liveoak.container.LiveOakSystem;
import io.liveoak.container.extension.application.InternalApplicationExtension;
import io.liveoak.container.tenancy.InternalApplication;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.mongo.launcher.MongoInstaller;
import io.liveoak.mongo.launcher.MongoLauncher;
import io.liveoak.spi.Services;
import io.liveoak.spi.client.Client;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.spi.util.ObjectMapperFactory;
import io.liveoak.testtools.resources.MockDirectAccessClient;
import io.liveoak.testtools.resources.MockSecurityClient;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.vertx.java.core.Vertx;

/**
 * @author Ken Finnigan
 */
public class AbstractTestCase {
    protected static File projectRoot;
    protected static File applicationDirectory;
    protected static LiveOakSystem system;
    protected static Client client;
    protected static Vertx vertx;

    protected static InternalApplication testApplication;
    protected static Set<InternalApplicationExtension> testAppExtensions = new HashSet<>();

    static {
        applicationDirectory = null;
    }

    protected static void setProjectRoot(Class<? extends AbstractTestCase> clazz) {
        String name = clazz.getName().replace(".", "/") + ".class";
        URL resource = clazz.getClassLoader().getResource(name);

        if (resource != null) {
            File current = new File(resource.getFile());

            while (current.exists()) {
                if (current.isDirectory()) {
                    if (new File(current, "pom.xml").exists()) {
                        projectRoot = current;
                        break;
                    }
                }

                current = current.getParentFile();
            }
        }

        if (projectRoot != null) {
            System.setProperty("user.dir", projectRoot.getAbsolutePath());
        }
    }

    @ClassRule
    public static ExternalResource liveOakSetup = new LiveOakSetup();

    @After
    public void postTestWait() throws InterruptedException {
        awaitStability();
    }

    protected JsonNode toJSON(String value) throws IOException {
        return ObjectMapperFactory.create().readTree(value);
    }

    public static void installTestApp() throws Exception {
        testApplication = system.applicationRegistry().createApplication("testApp", "Test Application", applicationDirectory);
        awaitStability();
    }

    protected static boolean awaitStability() throws InterruptedException {
        // Default all calls to a 5 second timeout if not specified
        return awaitStability(5, TimeUnit.SECONDS);
    }

    protected static boolean awaitStability(int timeout, TimeUnit unit) throws InterruptedException {
        return awaitStability(timeout, unit, new HashSet<>(), new HashSet<>());
    }

    protected static boolean awaitStability(long timeout, TimeUnit unit, Set<? super ServiceController<?>> failed, Set<? super ServiceController<?>> problem) throws InterruptedException {
        boolean stable = system.awaitStability(timeout, unit, failed, problem);
        if (!stable) {
            log.warn("awaitStability() may require an increased timeout duration.");
        }

        if (failed != null && !failed.isEmpty()) {
            Iterator<? super ServiceController<?>> failedIterator = failed.iterator();
            while (failedIterator.hasNext()) {
                ServiceController controller = (ServiceController) failedIterator.next();
                log.errorf(CONTROLLER_MESSAGE, controller.getName(), controller.getState(), controller.getSubstate(), controller.getMode());
                if (controller.getStartException() != null) {
                    controller.getStartException().printStackTrace();
                }
            }
        }

        if (problem != null && !problem.isEmpty()) {
            Iterator<? super ServiceController<?>> problemIterator = problem.iterator();
            while (problemIterator.hasNext()) {
                ServiceController controller = (ServiceController) problemIterator.next();
                log.errorf(CONTROLLER_MESSAGE, controller.getName(), controller.getState(), controller.getSubstate(), controller.getMode());
                if (controller.getStartException() != null) {
                    controller.getStartException().printStackTrace();
                }
            }
        }

        return stable;
    }

    protected static void loadExtension(String id, Extension ext) throws Exception {
        system.extensionInstaller().load(id, ext);
        awaitStability();
    }

    protected static void loadExtension(String id, Extension ext, ObjectNode extConfig) throws Exception {
        loadExtension(id, ext, extConfig, null);
    }

    protected static void loadExtension(String id, Extension ext, ObjectNode extConfig, ObjectNode instancesConfig) throws Exception {
        ObjectNode fullConfig = JsonNodeFactory.instance.objectNode();
        fullConfig.put("config", extConfig);

        if (instancesConfig != null) {
            fullConfig.put("instances", instancesConfig);
        }

        system.extensionInstaller().load(id, ext, fullConfig);
        awaitStability();
    }

    protected static InternalApplicationExtension installTestAppResource(String extId, String resourceId, ObjectNode resourceConfig) throws Exception {
        InternalApplicationExtension appExt = testApplication.extend(extId, resourceId, resourceConfig);
        testAppExtensions.add(appExt);
        awaitStability();
        return appExt;
    }

    protected static InternalApplicationExtension installTestAppResource(String extId, String resourceId, ResourceState resourceConfig) throws Exception {
        return installTestAppResource(extId, resourceId, ConversionUtils.convert(resourceConfig));
    }

    public static void removeTestAppResource(InternalApplicationExtension resource) throws InterruptedException {
        testAppExtensions.remove(resource);
        resource.remove();

        awaitStability();
    }

    public static void removeAllResources() throws InterruptedException {
        if (testAppExtensions != null && testAppExtensions.size() > 0) {
            for (InternalApplicationExtension extension : testAppExtensions) {
                extension.remove();
            }

            testAppExtensions.clear();
        }
        awaitStability();
    }

    static class LiveOakSetup extends ExternalResource {
        MongoLauncher mongoLauncher;
        int mongoPort = 27017;
        String mongoHost = "localhost";

        @Override
        protected void before() throws Throwable {
            startupMongo();
            startupLiveOak();
        }

        @Override
        protected void after() {
            shutdownMongo();
            shutdownLiveOak();
        }

        void startupMongo() throws Throwable {
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
                mongoLauncher.setPort(mongoPort);

                if (host != null) {
                    mongoHost = host;
                }
                mongoLauncher.setHost(mongoHost);

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
                while (!mongoLauncher.serverRunning(mongoHost, mongoPort, (e) -> {
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

        void startupLiveOak() throws Throwable {
            system = LiveOakFactory.create();
            client = system.client();
            vertx = system.vertx();

            system.serviceTarget().addService(Services.SECURITY_CLIENT, new ValueService<>(new ImmediateValue<>(new MockSecurityClient()))).install();
            system.serviceTarget().addService(Services.SECURITY_DIRECT_ACCESS_CLIENT, new ValueService<>(new ImmediateValue<>(new MockDirectAccessClient()))).install();

            awaitStability();

            system.applicationRegistry().createApplication(ZeroExtension.APPLICATION_ID, ZeroExtension.APPLICATION_NAME);

            system.awaitStability();
            log.debug("stable");
        }

        void shutdownLiveOak() {
            system.stop();
            if (testApplication != null) {
                testApplication.configurationFile().delete();
            }
        }

        void shutdownMongo() {
            if (mongoLauncher != null) {
                try {
                    mongoLauncher.stopMongo();

                    // wait for it to stop
                    long start = System.currentTimeMillis();
                    while (MongoLauncher.serverRunning(mongoHost, mongoPort, (e) -> {
                        if (System.currentTimeMillis() - start > 120000) throw new RuntimeException(e);
                    })) {

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
                } catch (Throwable t) {
                    log.error("MongoLauncherResource.after() error.", t);
                } finally {
                    mongoLauncher = null;
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

    private static String CONTROLLER_MESSAGE = "Controller %s is in State: %s, Substate: %s and Mode: %s";

    private static final Logger log = Logger.getLogger(AbstractTestCase.class);
}
