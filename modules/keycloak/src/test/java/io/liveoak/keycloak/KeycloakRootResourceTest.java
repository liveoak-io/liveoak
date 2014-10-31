package io.liveoak.keycloak;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.keycloak.extension.KeycloakExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.keycloak.representations.AccessToken;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakRootResourceTest extends AbstractKeycloakTest {

    private TokenUtil tokenUtil;

    @BeforeClass
    public static void loadExtensions() throws Exception {
        loadExtension("auth", new KeycloakExtension(), createTestConfig());
        installTestAppResource("auth", "auth", JsonNodeFactory.instance.objectNode());
    }

    @Before
    public void before() throws Exception {
        tokenUtil = new TokenUtil("liveoak-apps");
    }

    @Test
    public void testTokenInfo() throws Exception {
        RequestContext requestContext = new RequestContext.Builder().build();

        AccessToken token = tokenUtil.createToken();

        ResourceState returnedState = client.read(requestContext, "/testApp/auth/token-info/" + tokenUtil.toString(token));

        assertEquals(tokenUtil.realm(), returnedState.getProperty("realm"));
        assertEquals("user-id", returnedState.getProperty("subject"));
        assertEquals(token.getIssuedAt(), ((Date) returnedState.getProperty("issued-at")).getTime());

        List<String> roles = (List<String>) returnedState.getProperty("roles");
        assertEquals(3, roles.size());
        assertTrue(roles.contains("realm-role"));
        assertTrue(roles.contains("app-id/app-role"));
        assertTrue(roles.contains("app2-id/app-role"));
    }

}
