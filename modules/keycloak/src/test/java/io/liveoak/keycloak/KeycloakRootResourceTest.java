package io.liveoak.keycloak;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.liveoak.keycloak.extension.KeycloakExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.AccessToken;

import java.io.File;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakRootResourceTest extends AbstractKeycloakTest {

    private KeycloakRootResource keycloak;
    private TokenUtil tokenUtil;

    @Override
    public void loadExtensions() throws Exception {
        loadExtension( "auth", new KeycloakExtension(), createTestConfig());
        installResource( "auth", "auth", JsonNodeFactory.instance.objectNode() );
    }

    @Override
    protected File applicationDirectory() {
        return new File( this.projectRoot, "/src/test/resources" );
    }

    @Before
    public void before() throws Exception {
        tokenUtil = new TokenUtil((RealmModel) this.system.service( KeycloakServices.realmModel("testApp") ));
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
