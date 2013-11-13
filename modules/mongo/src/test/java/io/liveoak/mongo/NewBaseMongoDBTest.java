/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.liveoak.mongo;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import io.liveoak.container.SimpleConfig;
import io.liveoak.spi.Config;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.testtools.AbstractResourceTestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 */
public class NewBaseMongoDBTest extends AbstractResourceTestCase {

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
