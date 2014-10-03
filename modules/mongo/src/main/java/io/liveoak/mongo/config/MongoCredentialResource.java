package io.liveoak.mongo.config;

import com.mongodb.MongoCredential;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class MongoCredentialResource implements Resource {

    RootMongoConfigResource parent;
    MongoCredential credential;

    MongoCredentialResource(RootMongoConfigResource parent, MongoCredential credential) {
        this.parent = parent;
        this.credential = credential;
    }

    @Override
    public Resource parent() {
        return null;
    }

    @Override
    public String id() {
        return null;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept("mechanism", credential.getMechanism());
        sink.accept("username", credential.getUserName());

        //only the MONGODB CR has a password and database option, the GSS one only accepts a username
        if (credential.getMechanism().equals(MongoCredential.MONGODB_CR_MECHANISM)) {
            sink.accept("password", new String(credential.getPassword()));
            sink.accept("database", credential.getSource());
        }
        sink.complete();
    }


}
