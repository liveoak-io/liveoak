package io.liveoak.ups;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.container.zero.extension.ZeroExtension;
import io.liveoak.mongo.extension.MongoExtension;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractTestCaseWithTestApp;
import org.jboss.logging.Logger;
import org.junit.BeforeClass;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseUPSTestCase extends AbstractTestCaseWithTestApp {

    protected static final Logger log = Logger.getLogger(BaseUPSTestCase.class);

    protected static final String BASEPATH = "push";
    static final String ADMIN_PATH = "/" + ZeroExtension.APPLICATION_ID + "/applications/testApp/resources/" + BASEPATH;

    protected static DB db;
    protected static Mongo mongoClient;

    @BeforeClass
    public static void loadExtensions() throws Exception {
        //setup the mongo module that our tests will use
        String database = System.getProperty("mongo.db", "MONGOInternalTest");
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");
        log.debug("UPS Module Tests using Mongo on " + host + ":" + port + ", database: " + database);
        System.setProperty("mongo.db", database);
        System.setProperty("mongo.host", host);
        System.setProperty("mongo.port", "" + port);

        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.put("db", "MongoInternalTest");

        ObjectNode server = JsonNodeFactory.instance.objectNode();
        server.put("host", host);
        server.put("port", port);

        config.putArray("servers").add(server);
        loadExtension("mongo", new MongoExtension(), config);

        try {
            mongoClient = new MongoClient(host, port);
            db = mongoClient.getDB(database);
            db.setWriteConcern(WriteConcern.ACKNOWLEDGED);
            db.dropDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }


        loadExtension("ups", new UPSExtension());
        installTestAppResource("ups", BASEPATH, createConfig());
    }

    public static ResourceState createConfig() {
        ResourceState config = new DefaultResourceState();
        config.putProperty("upsURL", "http://localhost:8080/my_ups_server");
        config.putProperty("applicationId", "my-application-id");
        config.putProperty("masterSecret", "shhhh-its-a-secret");
        return config;
    }
}
