package io.liveoak.mongo.config;

import com.mongodb.WriteConcern;
import io.liveoak.spi.exceptions.InitializationException;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class WriteConcernResource implements Resource {

    WriteConcern writeConcern;
    RootMongoConfigResource parent;
    public static final String ID = "WriteConcern";

    public enum Options {
        W("w"),
        WTIMEOUT("wTimeout"),
        J("j"),
        FSYNC("fsync"),
        CONTINUEONERRORFORINSERT("continueOnErrorForInsert");

        private final String propertyName;

        Options(String propertyName) {
            this.propertyName = propertyName;
        }

        public String toString() {
            return propertyName;
        }

    }

    public WriteConcernResource(RootMongoConfigResource parent, WriteConcern writeConcern) {
        this.parent = parent;
        this.writeConcern = writeConcern;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(Options.W.propertyName, writeConcern.getWObject());
        sink.accept(Options.WTIMEOUT.propertyName, writeConcern.getWtimeout());
        sink.accept(Options.J.propertyName, writeConcern.getJ());
        sink.accept(Options.FSYNC.propertyName, writeConcern.getFsync());
        sink.accept(Options.CONTINUEONERRORFORINSERT.propertyName, writeConcern.getContinueOnErrorForInsert());
        sink.close();
    }

    public void updateWriteConcern(ResourceState state) throws Exception {
        Object w = state.getProperty(Options.W.propertyName);

        if (w == null) {
            w = writeConcern.getWObject();
        }

        Integer wTimeout = (Integer) state.getProperty(Options.WTIMEOUT.propertyName);
        if (wTimeout == null) {
            wTimeout = writeConcern.getWtimeout();
        }

        Boolean j = (Boolean) state.getProperty(Options.J.propertyName);
        if (j == null) {
            j = writeConcern.getJ();
        }

        Boolean fSync = (Boolean) state.getProperty(Options.FSYNC.propertyName);
        if (fSync == null) {
            fSync = writeConcern.getFsync();
        }

        Boolean continueOnErrorForInsert = (Boolean) state.getProperty(Options.CONTINUEONERRORFORINSERT.propertyName);
        if (continueOnErrorForInsert == null) {
            continueOnErrorForInsert = writeConcern.getContinueOnErrorForInsert();
        }

        WriteConcern updatedWriteConcern;
        if (w instanceof String) {
            updatedWriteConcern = new WriteConcern((String) w, wTimeout, fSync, j, continueOnErrorForInsert);
        } else if (w instanceof Integer) {
            updatedWriteConcern = new WriteConcern((Integer) w, wTimeout, fSync, j, continueOnErrorForInsert);
        } else {
            throw new InitializationException("Invalid value for parameter 'w'. Expecting a String or integer, received [" + w + "]");

        }

        this.writeConcern = updatedWriteConcern;
        this.parent.getMongoClient().setWriteConcern(updatedWriteConcern);
    }

    @Override
    public Resource parent() {
        return null;
    }

    @Override
    public String id() {
        return null;
    }
}
