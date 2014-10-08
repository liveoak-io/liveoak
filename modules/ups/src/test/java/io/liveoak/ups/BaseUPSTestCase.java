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
import io.liveoak.testtools.AbstractResourceTestCase;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseUPSTestCase extends AbstractResourceTestCase {

    protected final Logger log = Logger.getLogger(getClass());

    protected static final String BASEPATH = "push";
    static final String ADMIN_PATH = "/" + ZeroExtension.APPLICATION_ID + "/applications/testApp/resources/" + BASEPATH;

    protected DB db;
    protected Mongo mongoClient;

    @Override
    public void loadExtensions() throws Exception {
        //setup the mongo module that our tests will use
        String database = System.getProperty("mongo.db", "MONGOInternalTest");
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");
        log.debug("UPS Module Tests using Mongo on " + host + ":" + port + ", database: " + database);
        System.setProperty("mongo.db", database);
        System.setProperty("mongo.host", host);
        System.setProperty("mongo.port", "" + port);

        ObjectNode internalDatabase = JsonNodeFactory.instance.objectNode();
        internalDatabase.put("db", database);

        internalDatabase.put( "db", "MongoInternalTest");

        ObjectNode server = JsonNodeFactory.instance.objectNode();
        server.put( "host", host );
        server.put( "port", port );

        internalDatabase.putArray("servers").add( server );

        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.put("internal-database", internalDatabase);
        loadExtension( "mongo", new MongoExtension(), config );

        try {
            mongoClient = new MongoClient(host, port);
            db = mongoClient.getDB(database);
            db.setWriteConcern( WriteConcern.ACKNOWLEDGED);
            db.dropDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }



        loadExtension( "ups", new UPSExtension());
        installResource( "ups", BASEPATH, createConfig() );
    }

    public ResourceState createConfig() {
        ResourceState config = new DefaultResourceState();
        config.putProperty( "upsURL", "http://localhost:8080/my_ups_server" );
        config.putProperty( "applicationId", "my-application-id");
        config.putProperty( "masterSecret", "shhhh-its-a-secret");
        return config;
    }

    @Override
    public void setUpSystem() throws Exception {
        super.setUpSystem();
        db.dropDatabase();
    }
}
