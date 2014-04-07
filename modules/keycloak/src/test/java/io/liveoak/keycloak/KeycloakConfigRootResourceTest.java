package io.liveoak.keycloak;

import io.liveoak.common.DefaultRequestAttributes;
import io.liveoak.common.codec.DefaultResourceState;
import io.liveoak.keycloak.extension.KeycloakExtension;
import io.liveoak.spi.RequestContext;
import io.liveoak.spi.state.ResourceState;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakConfigRootResourceTest extends AbstractKeycloakTest {

    @Override
    public void loadExtensions() throws Exception {
        loadExtension("keycloak", new KeycloakExtension(), createTestConfig());
    }

    @Test
    public void readProperties() throws Exception {
        RequestContext requestContext = new RequestContext.Builder().requestAttributes(new DefaultRequestAttributes()).build();
        ResourceState returnedState = client.read(requestContext, "/admin/system/keycloak");

        Assert.assertEquals("http://localhost:8383/auth", returnedState.getProperty(KeycloakConfigRootResource.KEYCLOAK_URL));

        Map<String, String> keys = (Map<String, String>) returnedState.getProperty(KeycloakConfigRootResource.PUBLIC_KEYS);
        Assert.assertEquals(1, keys.size());
        Assert.assertEquals(TokenUtil.PUBLIC_KEY_PEM, keys.get("liveoak-apps"));

        Assert.assertEquals(false, returnedState.getProperty(KeycloakConfigRootResource.LOAD_PUBLIC_KEYS));
    }

    @Test
    public void updateProperties() throws Exception {
        ResourceState state = new DefaultResourceState();
        state.putProperty(KeycloakConfigRootResource.KEYCLOAK_URL, "http://test");

        ResourceState keys = new DefaultResourceState();
        keys.putProperty("test-realm1", "TESTKEY1");
        keys.putProperty("test-realm2", "TESTKEY2");
        state.putProperty(KeycloakConfigRootResource.PUBLIC_KEYS, keys);

        state.putProperty(KeycloakConfigRootResource.LOAD_PUBLIC_KEYS, true);

        RequestContext requestContext = new RequestContext.Builder().requestAttributes(new DefaultRequestAttributes()).build();
        ResourceState returnedState = client.update(requestContext, "/admin/system/keycloak", state);

        Assert.assertEquals(3, returnedState.getPropertyNames().size());
        Assert.assertEquals("http://test", returnedState.getProperty(KeycloakConfigRootResource.KEYCLOAK_URL));
        Assert.assertEquals(true, returnedState.getProperty(KeycloakConfigRootResource.LOAD_PUBLIC_KEYS));

        Map<String, String> returnedKeys = (Map<String, String>)returnedState.getProperty(KeycloakConfigRootResource.PUBLIC_KEYS);
        Assert.assertEquals(2, returnedKeys.size());
        Assert.assertEquals("TESTKEY1", returnedKeys.get("test-realm1"));
        Assert.assertEquals("TESTKEY2", returnedKeys.get("test-realm2"));
    }

}
