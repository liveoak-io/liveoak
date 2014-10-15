package io.liveoak.spi.extension;

/**
 * @author Bob McWhirter
 */
public interface Extension {

    void extend(SystemExtensionContext context) throws Exception;
    void extend(ApplicationExtensionContext context) throws Exception;
    void unextend(ApplicationExtensionContext context) throws Exception;

    default void instance(String id, SystemExtensionContext context) throws Exception {
    }
}
