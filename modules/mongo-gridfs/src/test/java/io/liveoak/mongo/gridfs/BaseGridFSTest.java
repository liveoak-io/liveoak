/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.liveoak.mongo.gridfs;

import java.util.UUID;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.After;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseGridFSTest extends AbstractResourceTestCase {

    protected String BASEPATH = "gridfs";

    protected static MongoClient mongoClient;
    protected static DB db;

    @Override
    public RootResource createRootResource() {
        return new GridFSRootResource(BASEPATH);
    }

    @Override
    public ResourceState createConfig() {
        String database = System.getProperty("mongo.db", "MongoGridFSTest_" + UUID.randomUUID());
        Integer port = new Integer(System.getProperty("mongo.port", "27019"));
        String host = System.getProperty("mongo.host", "localhost");

        ResourceState config = new DefaultResourceState();
        config.putProperty("db", database);
        config.putProperty("port", port);
        config.putProperty("host", host);

        try {
            mongoClient = new MongoClient(host, port);
            db = mongoClient.getDB(database);
            db.dropDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }

    @After
    public void tearDownDB() throws Exception {
        db.dropDatabase();
    }
}
