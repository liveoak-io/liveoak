package io.liveoak.mongo.service;

import com.mongodb.DB;
import com.mongodb.Mongo;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * @author Bob McWhirter
 */
public class DBService implements Service<DB> {

    public DBService(String orgId, String appId, String id) {
        this.orgId = orgId;
        this.appId = appId;
        this.id = id;
    }

    @Override
    public void start(StartContext context) throws StartException {
        this.db = mongoInjector.getValue().getDB( dbName() );
    }

    @Override
    public void stop(StopContext context) {
        this.db = null;
    }

    @Override
    public DB getValue() throws IllegalStateException, IllegalArgumentException {
        return this.db;
    }

    public Injector<Mongo> mongoInjector() {
        return this.mongoInjector;
    }

    protected String dbName() {
        return this.orgId + "-" + this.appId + "-" + this.id;
    }

    private String orgId;
    private String appId;
    private String id;

    private InjectedValue<Mongo> mongoInjector = new InjectedValue<>();

    private DB db;
}
