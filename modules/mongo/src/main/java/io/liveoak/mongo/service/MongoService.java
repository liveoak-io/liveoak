package io.liveoak.mongo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.Mongo;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

import java.net.UnknownHostException;

/**
 * @author Bob McWhirter
 */
public class MongoService implements Service<Mongo> {

    public MongoService() {
    }

    @Override
    public void start(StartContext context) throws StartException {
        try {
            String host = "localhost";
            int port = 27027;

            ObjectNode config = this.configurationInjector.getValue();
            if ( config.has( "host" ) ) {
                host = config.get( "host" ).asText();
            }
            if ( config.has( "port" ) ) {
                port = config.get( "port" ).asInt();
            }

            this.mongo = new Mongo( host, port );
        } catch (UnknownHostException e) {
            throw new StartException( e );
        }
    }

    @Override
    public void stop(StopContext context) {
        this.mongo.close();
    }

    @Override
    public Mongo getValue() throws IllegalStateException, IllegalArgumentException {
        return this.mongo;
    }

    public Injector<ObjectNode> configurationInjector() {
        return this.configurationInjector;
    }

    private InjectedValue<ObjectNode> configurationInjector = new InjectedValue<>();

    private Mongo mongo;
}

