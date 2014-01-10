package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.jboss.resteasy.jose.jws.JWSBuilder;
import org.jboss.resteasy.jwt.JsonSerialization;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.SkeletonKeyToken;

import java.security.PrivateKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakRootResourceTest extends AbstractResourceTestCase {

    private KeycloakRootResource keycloak;
    private TokenUtil tokenUtil;

    @Before
    public void before() throws Exception {
        tokenUtil = new TokenUtil(keycloak);
    }

    @Override
    public RootResource createRootResource() {
        keycloak = new KeycloakRootResource("auth");
        return keycloak;
    }

    @Override
    public ResourceState createConfig() {
        ResourceState config = super.createConfig();
        config.putProperty(KeycloakConfigResource.REALM_CONFIG, System.getProperty("user.dir") + "/src/test/resources/keycloak-config.json");
        return config;
    }

    @Test
    public void testTokenInfo() throws Exception {
        RequestContext requestContext = new RequestContext.Builder().build();

        SkeletonKeyToken token = tokenUtil.createToken();

        ResourceState returnedState = client.read(requestContext, "/auth/token-info/" + tokenUtil.toString(token));

        assertEquals(keycloak.getRealm(), returnedState.getProperty("realm"));
        assertEquals("user-id", returnedState.getProperty("subject"));
        assertEquals(token.getIssuedAt(), ((Date) returnedState.getProperty("issued-at")).getTime());

        List<String> roles = (List<String>) returnedState.getProperty("roles");
        assertEquals(3, roles.size());
        assertTrue(roles.contains("realm-role"));
        assertTrue(roles.contains("app-id/app-role"));
        assertTrue(roles.contains("app2-id/app-role"));
    }

}
