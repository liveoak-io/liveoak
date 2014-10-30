/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.mongo.gridfs.extension.GridFSExtension;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractTestCaseWithTestApp;
import org.jboss.logging.Logger;
import org.junit.BeforeClass;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseGridFSTest extends AbstractTestCaseWithTestApp {

    protected String BASEPATH = "gridfs";
    protected static final Logger log = Logger.getLogger(BaseGridFSTest.class);

    protected static Mongo mongoClient;
    protected static DB db;

    @BeforeClass
    public static void loadExtensions() throws Exception {
        loadExtension("gridfs", new GridFSExtension());
        installTestAppResource("gridfs", "gridfs", createConfig());
    }

    public static ResourceState createConfig() {
        String database = System.getProperty("mongo.db", "MongoControllerTest_" + UUID.randomUUID());
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");
        log.debug("Using Mongo on " + host + ":" + port + ", database: " + database);

        ResourceState config = new DefaultResourceState();
        config.putProperty("db", database);

        List<ResourceState> servers = new ArrayList<ResourceState>();
        ResourceState server = new DefaultResourceState();
        server.putProperty("port", port);
        server.putProperty("host", host);
        servers.add(server);
        config.putProperty("servers", servers);

        try {
            mongoClient = new MongoClient(host, port);
            db = mongoClient.getDB(database);
            db.setWriteConcern(WriteConcern.ACKNOWLEDGED);
            db.dropDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return config;
    }
}
