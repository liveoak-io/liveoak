/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.liveoak.security.policy.acl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import io.liveoak.common.DefaultResourceRequest;
import io.liveoak.common.DefaultResourceResponse;
import io.liveoak.common.DefaultSecurityContext;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.security.policy.acl.impl.AclPolicy;
import io.liveoak.security.policy.acl.impl.AclPolicyConfig;
import io.liveoak.security.policy.acl.impl.AclPolicyConfigurator;
import io.liveoak.security.policy.acl.impl.AutoRuleConfig;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.ResourceRequest;
import io.liveoak.spi.ResourceResponse;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyTestCase {

    private final Logger log = Logger.getLogger(AclPolicyTestCase.class);

    private AclPolicy aclPolicy;
    private MockAclTestStorageResource mockResource = new MockAclTestStorageResource("storage");
    private MongoClient mongoClient;
    private DB db;

    @Before
    public void before() {
        initMongo();

        List<AutoRuleConfig> autoRules = new ArrayList<>();

        // User can read,update or delete his own todos
        autoRules.add(createRule("/storage/todos", RequestType.READ.toString(), RequestType.UPDATE.toString(), RequestType.DELETE.toString()));

        // User can read his own chat messages. No update or delete them
        autoRules.add(createRule("/storage/chat", RequestType.READ.toString()));

        AclPolicyConfig config = new AclPolicyConfig();
        config.setAutoRules(autoRules);
        this.aclPolicy = new AclPolicy(db.getCollection("acl"));
        new AclPolicyConfigurator().configure(aclPolicy, config);
    }

    private AutoRuleConfig createRule(String collectionPath, String... autoAddedOwnerPermissions) {
        AutoRuleConfig autoRule = new AutoRuleConfig();
        autoRule.setResourcePath(collectionPath);
        autoRule.setAutoAddedOwnerPermissions(Arrays.asList(autoAddedOwnerPermissions));
        return autoRule;
    }

    private void initMongo() {
        String database = System.getProperty("mongo.db", "liveoak-acl");
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");
        log.debug("Using Mongo for ACL on " + host + ":" + port + ", database: " + database);

        try {
            mongoClient = new MongoClient(host, port);
            db = mongoClient.getDB(database);
            db.setWriteConcern(WriteConcern.ACKNOWLEDGED);
            db.dropDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void after() {
        mongoClient.close();
        log.debug("Closed mongo client");
    }

    @Test
    public void testAutocreateACE() {
        ResourceState createdState = aclPolicy.autocreateAce( createResourceResponse("/storage/todos", "123", "john123", "todo/user"));
        // Some testing of content of created ACEs
        Assert.assertEquals(1, createdState.members().size());
        ResourceState createdSt1 = createdState.members().get(0);
        Assert.assertEquals("john123", createdSt1.getProperty(AclPolicy.ACE_USER_ID));
        Assert.assertEquals("/storage/todos/123", createdSt1.getProperty(AclPolicy.ACE_RESOURCE_PATH));
        Assert.assertEquals("liveoak-apps", createdSt1.getProperty(AclPolicy.ACE_REALM));
        Assert.assertEquals(true, createdSt1.getProperty(AclPolicy.ACE_PERMITTED));
        Assert.assertEquals(3, ((String[])createdSt1.getProperty(AclPolicy.ACE_ACTIONS)).length);
    }

    @Test
    public void testTodomvc() {
        aclPolicy.autocreateAce( createResourceResponse("/storage/todos", "123", "john123"));
        aclPolicy.autocreateAce( createResourceResponse("/storage/todos", "456", "john123"));
        aclPolicy.autocreateAce( createResourceResponse("/storage/todos", "789", "peter123", "todos/user"));

        RequestContext testReq = createRequestContext("/storage/todos/123", "john123", RequestType.READ);
        Assert.assertEquals(AuthzDecision.ACCEPT, aclPolicy.isAuthorized(testReq));

        testReq = createRequestContext("/storage/todos/123", "john123", RequestType.UPDATE);
        Assert.assertEquals(AuthzDecision.ACCEPT, aclPolicy.isAuthorized(testReq));

        testReq = createRequestContext("/storage/todos/123", "john123", RequestType.DELETE);
        Assert.assertEquals(AuthzDecision.ACCEPT, aclPolicy.isAuthorized(testReq));

        testReq = createRequestContext("/storage/todos/123", "john123", RequestType.CREATE);
        Assert.assertEquals(AuthzDecision.IGNORE, aclPolicy.isAuthorized(testReq));

        testReq = createRequestContext("/storage/todos/456", "john123", RequestType.READ);
        Assert.assertEquals(AuthzDecision.ACCEPT, aclPolicy.isAuthorized(testReq));

        testReq = createRequestContext("/storage/todos/456", "peter123", RequestType.READ);
        Assert.assertEquals(AuthzDecision.IGNORE, aclPolicy.isAuthorized(testReq));

        testReq = createRequestContext("/storage/todos/789", "john123", RequestType.READ);
        Assert.assertEquals(AuthzDecision.IGNORE, aclPolicy.isAuthorized(testReq));

        testReq = createRequestContext("/storage/todos/789", "peter123", RequestType.READ);
        Assert.assertEquals(AuthzDecision.ACCEPT, aclPolicy.isAuthorized(testReq));
    }

    @Test
    public void testChat() {
        aclPolicy.autocreateAce( createResourceResponse("/storage/chat", "123", "john123"));
        aclPolicy.autocreateAce( createResourceResponse("/storage/chat", "456", "john123"));
        aclPolicy.autocreateAce( createResourceResponse("/storage/chat", "789", "peter123", "chat/user"));

        RequestContext testReq = createRequestContext("/storage/chat/123", "john123", RequestType.READ);
        Assert.assertEquals(AuthzDecision.ACCEPT, aclPolicy.isAuthorized(testReq));

        testReq = createRequestContext("/storage/chat/123", "john123", RequestType.UPDATE);
        Assert.assertEquals(AuthzDecision.IGNORE, aclPolicy.isAuthorized(testReq));

        testReq = createRequestContext("/storage/chat/123", "john123", RequestType.DELETE);
        Assert.assertEquals(AuthzDecision.IGNORE, aclPolicy.isAuthorized(testReq));

        testReq = createRequestContext("/storage/chat/123", "john123", RequestType.CREATE);
        Assert.assertEquals(AuthzDecision.IGNORE, aclPolicy.isAuthorized(testReq));

        testReq = createRequestContext("/storage/chat/456", "john123", RequestType.READ);
        Assert.assertEquals(AuthzDecision.ACCEPT, aclPolicy.isAuthorized(testReq));

        testReq = createRequestContext("/storage/chat/456", "peter123", RequestType.READ);
        Assert.assertEquals(AuthzDecision.IGNORE, aclPolicy.isAuthorized(testReq));

        testReq = createRequestContext("/storage/chat/789", "john123", RequestType.READ);
        Assert.assertEquals(AuthzDecision.IGNORE, aclPolicy.isAuthorized(testReq));

        testReq = createRequestContext("/storage/chat/789", "peter123", RequestType.READ);
        Assert.assertEquals(AuthzDecision.ACCEPT, aclPolicy.isAuthorized(testReq));
    }

    private ResourceResponse createResourceResponse(String parentResourcePath, String resourceId, String subject, String... roles) {
        RequestContext reqContext = createRequestContext(parentResourcePath, subject, RequestType.CREATE, roles);

        ResourceRequest req = new DefaultResourceRequest.Builder(reqContext.requestType(), reqContext.resourcePath())
                .requestContext(reqContext).build();

        DefaultResourceResponse resourceResponse = new DefaultResourceResponse(req, ResourceResponse.ResponseType.CREATED, new Resource() {

            @Override
            public Resource parent() {
                String parentId = new ResourcePath(parentResourcePath).tail().name();
                return mockResource.member(parentId);
            }

            @Override
            public String id() {
                return resourceId;
            }
        });
        return resourceResponse;
    }

    static RequestContext createRequestContext(String resourcePath, String subject, RequestType reqType, String... roles) {
        DefaultSecurityContext secCtx = new DefaultSecurityContext();
        secCtx.setSubject(subject);
        secCtx.setRoles(new HashSet<>(Arrays.asList(roles)));
        secCtx.setRealm("liveoak-apps");
        RequestContext reqContext = new RequestContext.Builder()
                .resourcePath(new ResourcePath(resourcePath))
                .requestType(reqType)
                .securityContext(secCtx);

        return reqContext;
    }

}
