package io.liveoak.security.policy.acl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.DefaultResourceParams;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.common.security.AuthzConstants;
import io.liveoak.common.security.AuthzDecision;
import io.liveoak.common.util.ConversionUtils;
import io.liveoak.common.util.ObjectMapperFactory;
import io.liveoak.interceptor.extension.InterceptorExtension;
import io.liveoak.mongo.extension.MongoExtension;
import io.liveoak.security.policy.acl.extension.SecurityACLPolicyExtension;
import io.liveoak.security.policy.acl.integration.AclPolicyConfigResource;
import io.liveoak.spi.RequestAttributes;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.RequestType;
import io.liveoak.spi.ResourceParams;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractTestCaseWithTestApp;
import io.liveoak.testtools.MockExtension;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AclPolicyRootResourceTest extends AbstractTestCaseWithTestApp {

    private static final Logger log = Logger.getLogger(AclPolicyRootResourceTest.class);

    @BeforeClass
    public static void loadExtensions() throws Exception {
        loadExtension("interceptor", new InterceptorExtension(), createInterceptorConfig());
        loadExtension("mongo", new MongoExtension(), createMongoConfig());
        loadExtension("acl-policy", new SecurityACLPolicyExtension());
        loadExtension("mock-storage", new MockExtension(MockAclTestStorageResource.class));

        installTestAppResource("acl-policy", "acl-policy", createPolicyConfig());
        installTestAppResource("mock-storage", "mock-storage", JsonNodeFactory.instance.objectNode());
    }

    private static ObjectNode createInterceptorConfig() {
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        ObjectNode aclUpdater = JsonNodeFactory.instance.objectNode()
                .put("interceptor-name", "acl-updater")
                .put("resource-path-prefix", "/testApp/mock-storage");
        config.putArray("local").add(aclUpdater);
        return config;
    }

    private static ObjectNode createPolicyConfig() throws Exception {
        ObjectMapper om = ObjectMapperFactory.create();
        ObjectNode objectNode = om.readValue(AclPolicyRootResourceTest.class.getClassLoader().getResourceAsStream("policy-config/acl-policy-config.json"), ObjectNode.class);
        return objectNode;
    }

    private static ObjectNode createMongoConfig() {
        String database = System.getProperty("mongo.db", "liveoak-acl");
        Integer port = new Integer(System.getProperty("mongo.port", "27017"));
        String host = System.getProperty("mongo.host", "localhost");
        log.debug("Using Mongo for ACL on " + host + ":" + port + ", database: " + database);

        ResourceState config = new DefaultResourceState();
        config.putProperty("db", database);

        List<ResourceState> servers = new ArrayList<ResourceState>();
        ResourceState server = new DefaultResourceState();
        server.putProperty("port", port);
        server.putProperty("host", host);
        servers.add(server);
        config.putProperty("servers", servers);
        return ConversionUtils.convert(config);
    }

    @Test
    public void aclPolicyTests() throws Exception {
        // Test #1 - Todomvc
        // Create some resources. AclUpdaterInterceptor should create ACE entries for those
        sendCreateRequest("/testApp/mock-storage/todos", "123", "john123");
        sendCreateRequest("/testApp/mock-storage/todos", "456", "john123");
        sendCreateRequest("/testApp/mock-storage/todos", "789", "peter123");

        // Test created resources
        RequestContext testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/123", "john123", RequestType.READ);
        assertAuthzDecision(testReq, AuthzDecision.ACCEPT);

        testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/123", "john123", RequestType.UPDATE);
        assertAuthzDecision(testReq, AuthzDecision.ACCEPT);

        testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/123", "john123", RequestType.DELETE);
        assertAuthzDecision(testReq, AuthzDecision.ACCEPT);

        testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/123", "john123", RequestType.CREATE);
        assertAuthzDecision(testReq, AuthzDecision.IGNORE);

        testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/456", "john123", RequestType.READ);
        assertAuthzDecision(testReq, AuthzDecision.ACCEPT);

        testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/456", "peter123", RequestType.READ);
        assertAuthzDecision(testReq, AuthzDecision.IGNORE);

        testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/789", "john123", RequestType.READ);
        assertAuthzDecision(testReq, AuthzDecision.IGNORE);

        testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/789", "peter123", RequestType.READ);
        assertAuthzDecision(testReq, AuthzDecision.ACCEPT);


        // Test #2 - Todomvc delete
        // Delete resource
        RequestContext deleteReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/123", "john123", RequestType.DELETE);
        client.delete(deleteReq, "/testApp/mock-storage/todos/123");

        // And assert that ACE entry was deleted as well and user doesn't have permission now
        testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/123", "john123", RequestType.READ);
        assertAuthzDecision(testReq, AuthzDecision.IGNORE);


        // Test #3 - Todomvc update config
        // First create some resource
        sendCreateRequest("/testApp/mock-storage/todos", "john-rw", "john123");

        // Update auto-rules config now. Allow just reading for owner in todos collection
        Map<String, List<String>> params = new HashMap<>();
        params.put("runtime", new ArrayList<>());
        ResourceParams resourceParams = DefaultResourceParams.instance(params);
        RequestContext reqCtx = new RequestContext.Builder().resourceParams(resourceParams).build();
        ResourceState config = client.read(reqCtx, "/admin/applications/testApp/resources/acl-policy");
        List<ResourceState> autoRules = (List<ResourceState>) config.getProperty(AclPolicyConfigResource.AUTO_RULES_PROPERTY);

        ResourceState todomvcRule = null;
        for (ResourceState rule : autoRules) {
            if (rule.getProperty("resourcePath").equals("/testApp/mock-storage/todos")) {
                todomvcRule = rule;
                break;
            }
        }
        Assert.assertNotNull(todomvcRule);
        todomvcRule.putProperty("autoAddedOwnerPermissions", Arrays.asList(RequestType.READ.toString()));

        // Update config with removed storage rule
        client.update(reqCtx, "/admin/applications/testApp/resources/acl-policy", config);

        // Create another resource. So this would be just readable by john123 due to updated autorules
        sendCreateRequest("/testApp/mock-storage/todos", "john-ro", "john123");

        // Test created resources
        testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/john-rw", "john123", RequestType.READ);
        assertAuthzDecision(testReq, AuthzDecision.ACCEPT);

        testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/john-rw", "john123", RequestType.UPDATE);
        assertAuthzDecision(testReq, AuthzDecision.ACCEPT);

        testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/john-rw", "john123", RequestType.DELETE);
        assertAuthzDecision(testReq, AuthzDecision.ACCEPT);

        testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/john-ro", "john123", RequestType.READ);
        assertAuthzDecision(testReq, AuthzDecision.ACCEPT);

        testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/john-ro", "john123", RequestType.UPDATE);
        assertAuthzDecision(testReq, AuthzDecision.IGNORE);

        testReq = AclPolicyTestCase.createRequestContext("/testApp/mock-storage/todos/john-ro", "john123", RequestType.DELETE);
        assertAuthzDecision(testReq, AuthzDecision.IGNORE);
    }

    private void sendCreateRequest(String collectionPath, String resourceId, String subject) throws Exception {
        RequestContext createReq1 = AclPolicyTestCase.createRequestContext(collectionPath, subject, RequestType.CREATE);
        client.create(createReq1, collectionPath, new DefaultResourceState(resourceId));
    }

    private void assertAuthzDecision(RequestContext reqCtxToCheck, AuthzDecision expectedDecision) throws Exception {
        RequestAttributes attribs = new DefaultRequestAttributes();
        attribs.setAttribute(AuthzConstants.ATTR_REQUEST_CONTEXT, reqCtxToCheck);
        RequestContext reqCtx = new RequestContext.Builder().requestAttributes(attribs).build();
        ResourceState state = client.read(reqCtx, "/testApp/acl-policy/authzCheck");
        String decision = (String) state.getProperty(AuthzConstants.ATTR_AUTHZ_POLICY_RESULT);
        Assert.assertNotNull(decision);
        Assert.assertEquals(expectedDecision, Enum.valueOf(AuthzDecision.class, decision));
    }

}
