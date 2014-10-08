package io.liveoak.mongo.config;

import com.mongodb.DB;
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
public class MongoSystemConfigResource implements ConfigResource, RootResource {

    private Resource parent;
    private String id;

    public static final String DATASTORES = "datastores";
    public static final String INTERNAL_DATABASE = "internal-database";

    MongoConfig internalConfig;
    DataStores dataStores;

    public MongoSystemConfigResource(String id) {
        this.id = id;
        dataStores = new DataStores(this);
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        updateDataSourcesProperties(ctx, state, responder, this.parent());
        responder.resourceUpdated(this);
    }

    @Override
    public void initializeProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        updateDataSourcesProperties(ctx, state, responder, this.parent());
        updateInternalDataBaseProperties(ctx, state, responder, this.parent());
        responder.resourceUpdated(this);
    }

    @Override
    public void readConfigProperties(RequestContext ctx, PropertySink sink, Resource resource) throws Exception {
        sink.accept(DATASTORES, dataStores);

        sink.accept(INTERNAL_DATABASE, internalConfig);
    }

    public void updateDataSourcesProperties(RequestContext ctx, ResourceState state, Responder responder, Resource resource) throws Exception {
        ResourceState datasources = state.getPropertyAsResourceState(DATASTORES);
        if (datasources != null) {
            for (String datasource : datasources.getPropertyNames()) {
                dataStores.addDataStore(datasource, new DataStore(this, datasources.getPropertyAsResourceState(datasource)));
            }
        }
    }

    public void updateInternalDataBaseProperties(RequestContext ctx, ResourceState state, Responder responder, Resource resource) throws Exception {
        ResourceState internalDatabase = state.getPropertyAsResourceState(INTERNAL_DATABASE);
        if (internalDatabase != null) {
            this.internalConfig = new MongoConfig(this, this, internalDatabase);
        }
    }

    public DB getSystemDatabase() {
        return internalConfig.getDB();
    }

    public DataStore getDataStore(String name) {
        return dataStores.getDataStore(name);
    }
}
