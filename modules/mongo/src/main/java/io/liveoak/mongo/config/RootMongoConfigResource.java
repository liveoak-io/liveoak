package io.liveoak.mongo.config;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import io.liveoak.spi.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.resource.config.ConfigResource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class RootMongoConfigResource implements ConfigResource, RootResource {

    private String id;
    private Resource parent;
    MongoClient mongoClient;
    DB db;

    WriteConcernResource writeConcernResource = null;
    ReadPreferenceResource readPreferenceResource = null;
    MongoClientOptionsResource mongoClientOptionsResource = null;
    List<ServerAddressResource> serverAddresses;
    List<MongoCredentialResource> credentials;

    public RootMongoConfigResource(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return this.id;
    }

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }

    public DB getDB() {
        return this.db;
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
    public void stop() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
        this.mongoClient = null;
    }

    @Override
    public void readConfigProperties(RequestContext ctx, PropertySink sink, Resource resource) throws Exception {
        sink.accept("db", db.getName());
        sink.accept("servers", getServerAddresses());
        sink.accept("credentials", getMongoCredentials());
        sink.accept(WriteConcernResource.ID, getWriteConcernResource());
        sink.accept(ReadPreferenceResource.ID, getReadPreferenceResource());
        sink.accept(MongoClientOptionsResource.ID, getMongoClientOptionsResource());
    }

    @Override
    public void updateConfigProperties(RequestContext ctx, ResourceState state, Responder responder, Resource resource) throws Exception {
        //required values: 'db' only
        String dbName = getPropertyAsString(state, "db");
        if (this.db == null && (dbName == null || dbName.isEmpty())) {
            throw new InitializationException("String configuration value required for 'db'");
        }

        Object mongoClientOptionsObject = state.getProperty("mongoClientOptions");
        MongoClientOptions mongoClientOptions = (this.mongoClient == null) ? MongoClientOptions.builder().description("liveoak").build() : this.mongoClient.getMongoClientOptions(); //the default MongoClientOptions
        if (mongoClientOptionsObject != null) {
            mongoClientOptions = getMongoClientOptionsResource().updateMongoClientOptions((ResourceState) mongoClientOptionsObject);
        }

        List<MongoCredential> credentials = getMongoCredentials(state);
        List<ServerAddress> serverAddresses = getServerAddresses(state);

        if (this.mongoClient == null) {
            if (serverAddresses == null || serverAddresses.isEmpty()) {
                this.mongoClient = new MongoClient(new ServerAddress(), credentials, mongoClientOptions);
            } else {
                this.mongoClient = new MongoClient(serverAddresses, credentials, mongoClientOptions);
            }
        } else {

            if (credentials == null) {
                credentials = this.mongoClient.getCredentialsList();
            }

            if (serverAddresses == null) {
                serverAddresses = this.mongoClient.getServerAddressList();
            }

            ReadPreference currentReadPreference = this.mongoClient.getReadPreference();
            WriteConcern currentWriteConcern = this.mongoClient.getWriteConcern();

            this.mongoClient = new MongoClient(serverAddresses, credentials, mongoClientOptions);
            this.mongoClient.setReadPreference(currentReadPreference);
            this.mongoClient.setWriteConcern(currentWriteConcern);
        }

        Object writeConcernState = state.getProperty(WriteConcernResource.ID);
        if (writeConcernState != null) {
            if (writeConcernState instanceof ResourceState) {
                getWriteConcernResource().updateWriteConcern((ResourceState) writeConcernState);
            } else {
                responder.invalidRequest("Invalid format for WriteConcern. Expecting a ResourceState, received [" + writeConcernState + "]");
            }
        }

        Object readPreferenceState = state.getProperty(ReadPreferenceResource.ID);
        if (readPreferenceState != null) {
            if (readPreferenceState instanceof ResourceState) {
                getReadPreferenceResource().updateReadPreference((ResourceState) readPreferenceState);
            }
        }

        Object mongoClientOptionsState = state.getProperty(MongoClientOptionsResource.ID);
        if (mongoClientOptions != null) {
            if (mongoClientOptionsState instanceof ResourceState) {
                getMongoClientOptionsResource().updateMongoClientOptions((ResourceState) mongoClientOptionsState);
            }
        }

        if (dbName != null) {
            DB db = mongoClient.getDB(dbName);
            if (db == null) {
                throw new InitializationException("Unknown database " + dbName);
            } else {
                this.db = db;
            }
        }

        //Load the configuration values into the resource
        getServerAddresses();
        getMongoCredentials();
        getWriteConcernResource();
        getReadPreferenceResource();
        getMongoClientOptionsResource();
    }

    private String getPropertyAsString(ResourceState state, String name) throws Exception {
        Object value = state.getProperty(name);
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        } else {
            throw new InitializationException("Configuration value \"" + name + "\" must be specified as a String.");
        }
    }

    private List<ServerAddress> getServerAddresses(ResourceState state) throws Exception {
        try {
            List<ServerAddress> serverAddresses = null;
            if (state.getProperty("servers") != null) {
                if (state.getProperty("servers") instanceof List) {
                    List servers = (List) state.getProperty("servers");
                    if (!servers.isEmpty()) {
                        serverAddresses = new ArrayList<ServerAddress>(servers.size());
                        for (Object server : servers) {
                            if (server instanceof ResourceState) {
                                ResourceState serverResourceState = (ResourceState) server;
                                String host = (String) serverResourceState.getProperty("host");
                                Integer port = (Integer) serverResourceState.getProperty("port");

                                if (port == null) {
                                    serverAddresses.add(new ServerAddress(host));
                                } else {
                                    serverAddresses.add(new ServerAddress(host, port));
                                }

                            } else {
                                throw new InitializationException("Configuration value \"servers\" is specified as an unexpected value");
                            }
                        }
                    }
                } else {
                    throw new InitializationException("Configuration value \"servers\" must be specified as a list.");
                }
            }
            return serverAddresses;
        } catch (Exception e) {
            throw new InitializationException("Exception occurred when trying to read the server addresses from the configuration", e);
        }
    }

    private List<MongoCredential> getMongoCredentials(ResourceState state) throws Exception {
        try {
            List<MongoCredential> mongoCredentials = null;
            Object credentialsProperty = state.getProperty("credentials");
            if (credentialsProperty != null) {
                if (credentialsProperty instanceof List) {
                    List credentials = (List) credentialsProperty;
                    mongoCredentials = new ArrayList<MongoCredential>(credentials.size());
                    for (Object credential : credentials) {
                        if (credential instanceof ResourceState) {
                            ResourceState credentialRS = (ResourceState) credential;

                            String username = (String) credentialRS.getProperty("username");
                            String password = (String) credentialRS.getProperty("password");
                            String mechanism = (String) credentialRS.getProperty("mechanism");
                            String database = (String) credentialRS.getProperty("database");

                            if (mechanism.equals(MongoCredential.MONGODB_CR_MECHANISM)) {
                                MongoCredential mongoCredential = MongoCredential.createMongoCRCredential(username, database, password.toCharArray());
                                mongoCredentials.add(mongoCredential);
                            } else {
                                MongoCredential mongoCredential = MongoCredential.createGSSAPICredential(username);
                                mongoCredentials.add(mongoCredential);
                            }

                        } else {
                            throw new InitializationException("Configuration value \"credentials\" is specified as an unexpected value");
                        }
                    }
                } else {
                    throw new InitializationException("Configuration value \"credentials\" must be specified as a list.");
                }
            }
            return mongoCredentials;
        } catch (Exception e) {
            throw new InitializationException("Exception occurred when trying to read the credentials from the configuration", e);
        }
    }

    private List<ServerAddressResource> getServerAddresses() {
        if (mongoClient != null) {
            serverAddresses = new ArrayList<ServerAddressResource>();
            for (ServerAddress serverAddress : mongoClient.getServerAddressList()) {
                serverAddresses.add(new ServerAddressResource(this, serverAddress));
            }
        }
        return serverAddresses;
    }

    private List<MongoCredentialResource> getMongoCredentials() {
        if (mongoClient != null) {
            credentials = new ArrayList<MongoCredentialResource>();
            for (MongoCredential credential : mongoClient.getCredentialsList()) {
                credentials.add(new MongoCredentialResource(this, credential));
            }
        }
        return credentials;
    }

    private WriteConcernResource getWriteConcernResource() {
        if (this.writeConcernResource == null && mongoClient != null) {
            writeConcernResource = new WriteConcernResource(this, mongoClient.getWriteConcern());
        }
        return this.writeConcernResource;
    }

    private ReadPreferenceResource getReadPreferenceResource() {
        if (this.readPreferenceResource == null && mongoClient != null) {
            this.readPreferenceResource = new ReadPreferenceResource(this, mongoClient.getReadPreference());
        }
        return this.readPreferenceResource;
    }

    private MongoClientOptionsResource getMongoClientOptionsResource() {
        if (this.mongoClientOptionsResource == null && mongoClient != null) {
            this.mongoClientOptionsResource = new MongoClientOptionsResource(this, mongoClient.getMongoClientOptions());
        }
        return this.mongoClientOptionsResource;
    }
}
