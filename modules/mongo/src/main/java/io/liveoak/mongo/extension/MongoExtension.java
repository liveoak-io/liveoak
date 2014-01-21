package io.liveoak.mongo.extension;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DB;
import com.mongodb.Mongo;
import io.liveoak.mongo.MongoServices;
import io.liveoak.mongo.service.DBService;
import io.liveoak.mongo.service.DropDBTask;
import io.liveoak.mongo.service.RootMongoResourceService;
import io.liveoak.mongo.service.MongoService;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.Extension;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.*;

/**
 * @author Bob McWhirter
 */
public class MongoExtension implements Extension {

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        ServiceTarget target = context.target();
        MongoService mongo = new MongoService();

        target.addService(MongoServices.mongo(context.id()), mongo)
                .addDependency(context.configurationServiceName(), ObjectNode.class, mongo.configurationInjector())
                .install();
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        setUpDatabase(context);
        setUpResource(context);
    }

    protected void setUpDatabase(ApplicationExtensionContext context) throws Exception {
        String orgId = context.application().organization().id();
        String appId = context.application().id();
        String id = context.id();

        ServiceTarget target = context.target();
        ServiceName name = MongoServices.db(orgId, appId, id);

        DBService db = new DBService(orgId, appId, id);
        target.addService(name, db)
                .addDependency(MongoServices.mongo(context.id()), Mongo.class, db.mongoInjector())
                .install();
    }

    protected void setUpResource(ApplicationExtensionContext context) throws Exception {
        String orgId = context.application().organization().id();
        String appId = context.application().id();
        String id = context.id();

        ServiceTarget target = context.target();
        ServiceName name = MongoServices.db(orgId, appId, id);

        RootMongoResourceService resource = new RootMongoResourceService(id);
        target.addService(name.append("resource"), resource)
                .addDependency(name, DB.class, resource.dbInjector())
                .install();

        context.mountPublic(name.append("resource"));
    }

    public void unextend(ApplicationExtensionContext context) throws Exception {

        String orgId = context.application().organization().id();
        String appId = context.application().id();
        String id = context.id();

        ServiceTarget target = context.target();

        ServiceName name = MongoServices.db(orgId, appId, id);
        DropDBTask dropDb = new DropDBTask();

        target.addService(name.append("drop"), dropDb)
                .addDependency(name, DB.class, dropDb.dbInjector())
                .install();
    }

}
