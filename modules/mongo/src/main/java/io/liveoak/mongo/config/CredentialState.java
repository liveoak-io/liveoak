package io.liveoak.mongo.config;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.MongoCredential;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class CredentialState extends EmbeddedConfigResource {

    public static final String ID = "credentials";

    public static final String MECHANISM = "mechanism";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String DB = "database";

    private MongoCredential mongoCredential;

    public CredentialState(Resource parent, ResourceState resourceState) throws Exception {
        super(parent);

        String username = resourceState.getProperty(USERNAME, false, String.class);
        String password = resourceState.getProperty(PASSWORD, false, String.class);
        String mechanism = resourceState.getProperty(MECHANISM, false, String.class);
        String database = resourceState.getProperty(DB, false, String.class);

        if (username != null && !username.isEmpty()) {

            MongoCredential credential;
            if (mechanism.equals(MongoCredential.MONGODB_CR_MECHANISM)) {
                credential = MongoCredential.createMongoCRCredential(username, database, password.toCharArray());
            } else {
                credential = MongoCredential.createGSSAPICredential(username);
            }

            this.mongoCredential = credential;
        } else {
            this.mongoCredential = null;
        }
    }

    public CredentialState(Resource parent, MongoCredential credential) {
        super(parent);
        this.mongoCredential = credential;
    }


    @Override
    public Map<String, ?> properties(RequestContext ctx) throws Exception {
        Map properties = new HashMap<>();
        properties.put(MECHANISM, mongoCredential.getMechanism());
        properties.put(USERNAME, mongoCredential.getUserName());

        //only the MONGODB CR has a password and database option, the GSS one only accepts a username
        if (mongoCredential.getMechanism().equals(MongoCredential.MONGODB_CR_MECHANISM)) {
            properties.put(PASSWORD, new String(mongoCredential.getPassword()));
            properties.put(DB, mongoCredential.getSource());
        }

        return properties;
    }

    public MongoCredential getMongoCredential() {
        return mongoCredential;
    }


}
