package io.liveoak.keycloak;

import io.liveoak.keycloak.extension.KeycloakExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.state.ResourceState;
import io.liveoak.testtools.AbstractResourceTestCase;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.SkeletonKeyToken;

import java.io.File;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakRootResourceTest extends AbstractResourceTestCase {

    private KeycloakRootResource keycloak;
    private TokenUtil tokenUtil;

    @Override
    public void loadExtensions() throws Exception {
        loadExtension( "auth", new KeycloakExtension() );
    }

    @Override
    protected File applicationDirectory() {
        return new File( this.projectRoot, "/src/test/resources" );
    }

    @Before
    public void before() throws Exception {
        tokenUtil = new TokenUtil((RealmModel) this.system.service( KeycloakServices.realmModel("testOrg", "testApp") ));
    }

    @Test
    public void testTokenInfo() throws Exception {
        RequestContext requestContext = new RequestContext.Builder().build();

        SkeletonKeyToken token = tokenUtil.createToken();

        ResourceState returnedState = client.read(requestContext, "/testOrg/testApp/auth/token-info/" + tokenUtil.toString(token));

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
