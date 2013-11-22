/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.mongo;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.liveoak.container.SimpleConfig;
import io.liveoak.spi.Config;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.testtools.AbstractResourceTestCase;

import java.util.UUID;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class BaseMongoDBTest extends AbstractResourceTestCase {

    protected String BASEPATH = "storage";

    protected static MongoClient mongoClient;
    protected static DB db;

    @Override
    public RootResource createRootResource() {
        return new RootMongoResource(BASEPATH);
    }

    @Override
    public Config createConfig() {
        String database = System.getProperty("mongo.db", "MongoControllerTest_" + UUID.randomUUID());
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");

        SimpleConfig config = new SimpleConfig();
        config.put("db", database);
        config.put("port", port);
        config.put("host", host);

        try {
            mongoClient = new MongoClient(host, port);
            db = mongoClient.getDB(database);
            db.dropDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }
}
