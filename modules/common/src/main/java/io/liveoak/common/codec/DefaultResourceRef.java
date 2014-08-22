package io.liveoak.common.codec;

import java.net.URI;
import java.net.URISyntaxException;

import io.liveoak.spi.ResourcePath;
import io.liveoak.spi.state.ResourceRef;
import io.liveoak.spi.state.ResourceState;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class DefaultResourceRef extends DefaultResourceState implements ResourceRef {
    private ResourcePath resourcePath;
    private ResourceRef parent;

    public DefaultResourceRef(ResourceRef parent, String id) throws URISyntaxException {
        this.resourcePath = new ResourcePath(parent.uri() + "/" + id);
        this.parent = parent;
        uri(new URI(resourcePath.toString()));
    }

    public DefaultResourceRef(ResourcePath resourcePath) throws URISyntaxException {
        this.resourcePath = resourcePath;
        uri(new URI(resourcePath.toString()));
    }

    public DefaultResourceRef(URI uri) {
        this.resourcePath = new ResourcePath(uri.getPath().toString());
        uri(uri);
    }

    public ResourcePath resourcePath() {
        return new ResourcePath(resourcePath);
    }

    public ResourceRef parent() {
        if (parent == null) {
            try {
                parent = new DefaultResourceRef(resourcePath.parent());
            } catch (URISyntaxException e) {
                throw new RuntimeException("Failed to create URI based on resourcePath: " + resourcePath.parent(), e);
            }
        }
        return parent;
    }

    @Override
    public void putProperty(String name, Object value) {
        throw new UnsupportedOperationException("Can't set properties");
    }

    @Override
    public void addMember(ResourceState member) {
        throw new UnsupportedOperationException("Can't add members");
    }

    @Override
    public String toString() {
        return "[DefaultResourceRef: uri=" + this.uri() + "]";
    }
}
