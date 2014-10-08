package io.liveoak.mongo.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class DataStore extends EmbeddedConfigResource{

    //Property keys
    public static final String SERVERS = "servers";

    MongoClient mongoClient;

    public DataStore(Resource parent, ResourceState resourceState) throws Exception{
        super(parent);
        properties(resourceState);
    }

    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map properties = new HashMap<>();

        List<Resource> addressStates = new ArrayList<>();
        for (ServerAddress serverAddress : mongoClient.getAllAddress()) {
            addressStates.add(new ServerAddressState(this, serverAddress));
        }
        properties.put(SERVERS, addressStates);

        List<Resource> credentialStates = new ArrayList<>();
        for (MongoCredential mongoCredential: mongoClient.getCredentialsList()) {
            credentialStates.add(new CredentialState(this, mongoCredential));
        }
        properties.put(CredentialState.ID, credentialStates);

        WriteConcernState writeConcern = new WriteConcernState(this, mongoClient.getWriteConcern());
        properties.put(WriteConcernState.ID, writeConcern);

        ReadPreferenceState readPreference = new ReadPreferenceState(this, mongoClient.getReadPreference());
        properties.put(ReadPreferenceState.ID, readPreference);

        MongoClientOptionsState mongoClientOptions = new MongoClientOptionsState(this, mongoClient.getMongoClientOptions());
        properties.put(MongoClientOptionsState.ID, mongoClientOptions);

        return properties;
    }

    @Override
    public void properties(ResourceState datastorestate) throws Exception {
        List<ServerAddress> serverAddresses = new ArrayList<>();
        List<ResourceState> servers = datastorestate.getProperty(SERVERS, false, List.class);
        if (servers != null) {
            for (ResourceState server : servers) {
                ServerAddressState address = new ServerAddressState(this,server);
                serverAddresses.add(address.getServerAddress());
            }
        }

        List<MongoCredential> mongoCredentials = new ArrayList<>();
        List<ResourceState> credentials = datastorestate.getProperty(CredentialState.ID, false, List.class);
        if (credentials != null) {
            for (ResourceState credential : credentials) {
                CredentialState cred = new CredentialState(this, credential);
                mongoCredentials.add(cred.getMongoCredential());
            }
        }

        ResourceState mongoClientOptionsState = datastorestate.getProperty(MongoClientOptionsState.ID, false, ResourceState.class);
        //Note: the MongoClientOptionsState handles the null situation itself.
        MongoClientOptions mongoClientOptions = new MongoClientOptionsState(this,mongoClientOptionsState).getMongoClientOptions();

        MongoClient mongoClient;
        if (serverAddresses.isEmpty()) {
            mongoClient = new MongoClient(new ServerAddress(), mongoCredentials, mongoClientOptions);
        } else {
            mongoClient = new MongoClient(serverAddresses, mongoCredentials, mongoClientOptions);
        }

        ResourceState writeConcernState = datastorestate.getProperty(WriteConcernState.ID, false, ResourceState.class);
        if (writeConcernState != null) {
            com.mongodb.WriteConcern writeConcern = new WriteConcernState(this, writeConcernState).getWriteConcern();
            mongoClient.setWriteConcern(writeConcern);
        }

        ResourceState readPreferenceState = datastorestate.getProperty(ReadPreferenceState.ID, false, ResourceState.class);
        if (readPreferenceState != null) {
            com.mongodb.ReadPreference readPreference = new ReadPreferenceState(this, readPreferenceState).getReadPreference();
            mongoClient.setReadPreference(readPreference);
        }

        this.mongoClient = mongoClient;
    }
}

