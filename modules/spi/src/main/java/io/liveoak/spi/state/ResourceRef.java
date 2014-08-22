package io.liveoak.spi.state;


import io.liveoak.spi.ResourcePath;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public interface ResourceRef extends ResourceState {

    ResourcePath resourcePath();
}
