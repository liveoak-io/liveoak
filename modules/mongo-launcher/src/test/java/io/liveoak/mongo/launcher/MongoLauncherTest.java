package io.liveoak.mongo.launcher;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class MongoLauncherTest {

    private static final Logger log = Logger.getLogger(MongoLauncherTest.class);

    protected File projectRoot;

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

    @Test
    public void testStartStop() throws IOException {

        MongoLauncher mongo = new MongoLauncher();

        File target = new File(this.projectRoot, "target");
        File dbFile = new File(target, "mongo-data");
        if (!dbFile.isDirectory() && !dbFile.mkdirs()) {
            Assert.fail("Failed to create mongo-data directory: " + dbFile);
        }
        mongo.setDbPath(dbFile.getAbsolutePath());

        // todo try find path to mongod
        String mongod = findMongodPath();
        if (mongod == null) {
            // we can't perform this test without mongod
            log.warn("No mongod found. Can't test MongoLauncher.");
            return;
        }

        mongo.setMongodPath(mongod);
        File pidFile = new File(target, "mongod.pid");
        mongo.setPidFilePath(pidFile.getAbsolutePath());
        mongo.setLogPath(new File(target, "mongod.log").getAbsolutePath());
        mongo.addExtraArgs("-v");
        mongo.addExtraArgs("--maxConns 5");
        int mongoPort = 27026;
        mongo.setPort(mongoPort);

        // check that mongod is not yet running
        if (checkPortReady(mongoPort, 2000)) {
            Assert.fail("Designated mongod port (" + mongoPort + ") is already taken. Mongod already running?" + (pidFile.isFile() ? "(PID file exists: " + pidFile + ")" : "") );
        }

        mongo.startMongo();

        // give it a few seconds to start
        checkPortReady(mongoPort, 120000);

        DBCollection collection = null;
        try {

            Assert.assertTrue("Mongod started", mongo.started());
            //Assert.assertTrue("pid file exists", pidFile.isFile());

            MongoClient client = new MongoClient("localhost", mongoPort);
            String dbName = "test";
            DB db = client.getDB(dbName);
            if (db == null) {
                Assert.fail("Unknown database " + dbName);
            }

            String collectionName = "test_" + System.currentTimeMillis();
            collection = db.getCollection(collectionName);
            BasicDBObject obj = new BasicDBObject("field1", "value1").append("field2", "value2");

            WriteResult result = collection.insert(obj);
            Assert.assertNull("No insert() error", result.getError());

            result = collection.remove(obj);
            Assert.assertNull("No remove() error", result.getError());

        } finally {
            if (collection != null) {
                collection.drop();
            }

            mongo.stopMongo();
        }
    }

    private boolean checkPortReady(int port, int timeout) throws IOException {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeout) {
            try {
                Socket socket = new Socket();
                socket.setSoTimeout(timeout);
                socket.connect(new InetSocketAddress("localhost", port));
                socket.close();
                return true;
            } catch (ConnectException ignored) {
                if (System.currentTimeMillis() - start < timeout) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private String findMongodPath() {
        String path = MongoLauncher.findMongod();
        if (path != null && new File(path).isFile()) {
            log.info("Using mongod at: " + path);
            return path;
        }
        return null;
    }
}
