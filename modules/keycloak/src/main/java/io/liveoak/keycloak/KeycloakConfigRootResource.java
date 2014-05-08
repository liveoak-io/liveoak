package io.liveoak.keycloak;

import io.liveoak.spi.RequestContext;
import io.liveoak.spi.resource.RootResource;
import io.liveoak.spi.resource.async.PropertySink;
import io.liveoak.spi.resource.async.Resource;
import io.liveoak.spi.resource.async.ResourceSink;
import io.liveoak.spi.resource.async.Responder;
import io.liveoak.spi.state.ResourceState;
import org.jboss.logging.Logger;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class KeycloakConfigRootResource implements RootResource {

    private static final Logger log = Logger.getLogger("io.liveoak.keycloak");
    public static final String LOAD_PUBLIC_KEYS = "load-public-keys";
    public static final String KEYCLOAK_URL = "keycloak-url";
    public static final String PUBLIC_KEYS = "public-keys";

    private Resource parent;
    private final String id;
    private KeycloakConfig config;

    public KeycloakConfigRootResource(String id, KeycloakConfig config) {
        this.id = id;
        this.config = config;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public void parent(Resource parent) {
        this.parent = parent;
    }

    @Override
    public Resource parent() {
        return this.parent;
    }

    @Override
    public void readProperties(RequestContext ctx, PropertySink sink) throws Exception {
        sink.accept(KEYCLOAK_URL, config.getBaseUrl());
        sink.accept(PUBLIC_KEYS, config.getPublicKeyPems());
        sink.accept(LOAD_PUBLIC_KEYS, config.isLoadKeys());

        sink.close();
    }

    @Override
    public void updateProperties(RequestContext ctx, ResourceState state, Responder responder) throws Exception {
        Set<String> keys = state.getPropertyNames();

        if (keys.contains(KEYCLOAK_URL)) {
            config.setBaseUrl((String) state.getProperty(KEYCLOAK_URL));
        }

        Map<String, String> publicKeys = new Hashtable<>();
        if (keys.contains(PUBLIC_KEYS)) {
            ResourceState k = (ResourceState) state.getProperty(PUBLIC_KEYS);
            for (String r : k.getPropertyNames()) {
                publicKeys.put(r, (String) k.getProperty(r));
            }
        }
        config.setPublicKeyPems(publicKeys);

        config.setLoadKeys(keys.contains(LOAD_PUBLIC_KEYS) ? (boolean) state.getProperty(LOAD_PUBLIC_KEYS) : false);

        responder.resourceUpdated(this);
    }

    public Logger logger() {
        return log;
    }

}

