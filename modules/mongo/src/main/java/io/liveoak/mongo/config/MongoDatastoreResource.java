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
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.SynchronousResource;
import io.liveoak.spi.resource.async.DelegatingResponder;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoDatastoreResource implements RootResource, SynchronousResource {

    //Property keys
    public static final String SERVERS = "servers";

    MongoClient mongoClient;
    Resource parent;
    String id;

    public MongoDatastoreResource (String id) {
        this.id = id;
    }

    public MongoDatastoreResource(Resource parent, ResourceState state) throws Exception {
        this.parent = parent;
        properties(state);
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
                if (cred.getMongoCredential() != null) {
                    mongoCredentials.add(cred.getMongoCredential());
                }
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

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void delete(RequestContext ctx, Responder responder) throws Exception {
        parent.deleteMember(ctx, this.id(), new deleteDelegatingResponse(responder));
    }

    private class deleteDelegatingResponse extends DelegatingResponder {

        public deleteDelegatingResponse(Responder delegate) {
            super(delegate);
        }

        @Override
        public void resourceDeleted(Resource resource) {
            super.resourceDeleted(resource);
            mongoClient.close();
        }
    }
}
