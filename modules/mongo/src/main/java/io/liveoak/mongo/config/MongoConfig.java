package io.liveoak.mongo.config;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.DB;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.exceptions.PropertyException;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoConfig extends EmbeddedConfigResource {

    public static final String DATABASE = "db";
    public static final String DATASTORE = "datastore";

    private String databaseName;
    private String datastoreName;
    private MongoDatastoreResource dataStore;

    private MongoDatastoresRegistry mongoDatastoresRegistry;

    public MongoConfig(Resource parent, MongoDatastoresRegistry mongoSystemConfigResource, ResourceState resourceState, boolean init) throws Exception {
        super(parent);
        this.mongoDatastoresRegistry = mongoSystemConfigResource;
        generateDataStore(resourceState, init);
    }

    @Override
    public void properties(ResourceState configState) throws Exception {
        generateDataStore(configState, false);
    }

    public void generateDataStore(ResourceState resourceState, Boolean init) throws Exception {
        String database = resourceState.getProperty(DATABASE, true, String.class);
        String datastore = resourceState.getProperty(DATASTORE, false, String.class);

        this.databaseName = encodeDatabaseName(database);

        if (resourceState.getPropertyNames().contains(DATASTORE) && (datastore == null || datastore.isEmpty() )) {
            throw new PropertyException("A datastore cannot accept a null value.");
        }

        // if we don't have a datastore specified, but no servers is specified, then use the default 'module' datastore
        if (datastore == null && !resourceState.getPropertyNames().contains(MongoDatastoreResource.SERVERS)) {
            datastore = "module";
        }

        if (datastore != null && mongoDatastoresRegistry != null) {
            if (mongoDatastoresRegistry.getDataStore(datastore) != null || init) {
                this.datastoreName = datastore;
            } else {
                throw new PropertyException("No datastore named '" + datastore + "' exists.");
            }
        } else if (datastore != null && mongoDatastoresRegistry == null) {
            throw new PropertyException("DataStores not supported for this resource.");
        } else {
            this.dataStore = new MongoDatastoreResource(this, resourceState);
        }
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map properties = new HashMap<>();

        if (datastoreName != null) {
            properties.put(DATASTORE, datastoreName);
        } else {
           properties = dataStore.properties(ctx);
        }

        properties.put(DATABASE, decodeDatabaseName(this.databaseName));

        return properties;
    }

    public DB getDB() {
        if (datastoreName != null && mongoDatastoresRegistry != null) {
            return mongoDatastoresRegistry.getDataStore(datastoreName).mongoClient.getDB(databaseName);
        } else if (dataStore != null){
            return dataStore.mongoClient.getDB(databaseName);
        } else {
            return null;
        }
    }

    public void close() {
        if (datastoreName == null && dataStore != null) {
            dataStore.mongoClient.close();
        }
    }


    //TODO: fix this up a bit better
    private String encodeDatabaseName(String databaseName) {
        if (databaseName == null || databaseName.isEmpty()) {
            return databaseName;
        }

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i< databaseName.length(); i++){
            String character = databaseName.substring(i, i+1);
            switch (character) {
                case "/":
                    buffer.append("%2F");
                    break;
                case "\\":
                    buffer.append("%5C");
                    break;
                case ".":
                    buffer.append("%2E");
                    break;
                case " ":
                    buffer.append("%20");
                    break;
                case "*":
                    buffer.append("%2A");
                    break;
                case "<":
                    buffer.append("%3C");
                    break;
                case ">":
                    buffer.append("%3E");
                    break;
                case ":":
                    buffer.append("%3A");
                    break;
                case "?":
                    buffer.append("%3F");
                    break;
                default:
                    buffer.append(character);
            }
        }

        return buffer.toString();
    }

    //TODO: fix this up a bit better
    private String decodeDatabaseName(String databaseName) {
        String decoded = databaseName.replace("%2F", "/");
        decoded = decoded.replace("%5C", "\\");
        decoded = decoded.replace("%2E", ".");
        decoded = decoded.replace("%20", " ");
        decoded = decoded.replace("%2A", "*");
        decoded = decoded.replace("%3C", "<");
        decoded = decoded.replace("%3E", ">");
        decoded = decoded.replace("%3A", ":");
        decoded = decoded.replace("%3F", "?");

        return decoded;
    }
}
