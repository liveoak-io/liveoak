package io.liveoak.mongo.gridfs.extension;

import com.mongodb.DB;
import io.liveoak.mongo.MongoServices;
import io.liveoak.mongo.extension.MongoExtension;
import io.liveoak.mongo.gridfs.service.GridFSResourceService;
import io.liveoak.mongo.gridfs.service.TmpDirService;
import io.liveoak.mongo.service.RootMongoResourceService;
import io.liveoak.spi.LiveOak;
import io.liveoak.spi.extension.ApplicationExtensionContext;
import io.liveoak.spi.extension.SystemExtensionContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.vertx.java.core.Vertx;

import java.io.File;

/**
 * @author Bob McWhirter
 */
public class GridFSExtension extends MongoExtension {

    public static ServiceName TMP_DIR = LiveOak.LIVEOAK.append( "gridfs", "tmpdir" );

    @Override
    public void extend(SystemExtensionContext context) throws Exception {
        // setup Mongo client
        super.extend(context);

        ServiceTarget target = context.target();
        target.addService( TMP_DIR, new TmpDirService() )
                .install();
    }

    @Override
    public void extend(ApplicationExtensionContext context) throws Exception {
        super.extend( context );
    }

    @Override
    protected void setUpResource(ApplicationExtensionContext context) throws Exception {
        String orgId = context.application().organization().id();
        String appId = context.application().id();
        String id = context.id();

        ServiceTarget target = context.target();
        ServiceName name = MongoServices.db(orgId, appId, id);

        GridFSResourceService resource = new GridFSResourceService( context.id() );
        target.addService( name.append( "resource" ), resource )
                .addDependency(name, DB.class, resource.dbInjector())
                .addDependency( TMP_DIR, File.class, resource.tmpDirInjector() )
                .addDependency(LiveOak.VERTX, Vertx.class, resource.vertxInjector())
                .install();

        context.mountPublic(name.append("resource"));
    }

    @Override
    public void unextend(ApplicationExtensionContext context) throws Exception {
        // destroy the underlying DB
        super.unextend(context);
    }
}
